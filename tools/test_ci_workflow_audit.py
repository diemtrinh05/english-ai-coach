from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

from tools import ci_workflow_audit


class CiWorkflowAuditTests(unittest.TestCase):

    def test_canonical_integration_pattern_selector_passes(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8")

        self.assertIn(ci_workflow_audit.CANONICAL_UNIT_SELECTOR, workflow)
        self.assertIn(ci_workflow_audit.CANONICAL_INTEGRATION_SELECTOR, workflow)
        self.assertIn("persist-credentials: false", workflow)
        self.assertIn("actions/setup-node@820762786026740c76f36085b0efc47a31fe5020", workflow)
        self.assertIn("working-directory: admin-web", workflow)
        for command in ci_workflow_audit.EXPECTED_ADMIN_COMMANDS:
            self.assertIn(command, workflow)
        self.assertEqual([], ci_workflow_audit.audit())

    def test_rejects_missing_setup_node(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            "      - name: Setup Node\n"
            "        uses: actions/setup-node@820762786026740c76f36085b0efc47a31fe5020 # v7.0.0\n"
            "        with:\n"
            "          node-version: \"24\"\n"
            "          cache: npm\n"
            "          cache-dependency-path: admin-web/package-lock.json\n\n",
            "",
            1,
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("GATE_ORDER_DRIFT:") for issue in issues))
        self.assertTrue(any(issue.startswith("ACTION_COUNT:") for issue in issues))

    def test_rejects_unpinned_setup_node(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            "actions/setup-node@820762786026740c76f36085b0efc47a31fe5020",
            "actions/setup-node@v7",
            1,
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("UNPINNED_ACTION:") for issue in issues))

    def test_rejects_wrong_node_version(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            '          node-version: "24"',
            '          node-version: "22"',
            1,
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("NODE_VERSION:") for issue in issues))

    def test_rejects_missing_admin_working_directory(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            "        working-directory: admin-web\n",
            "",
            1,
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("ADMIN_WORKING_DIRECTORY:") for issue in issues))

    def test_rejects_admin_continue_on_error(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            "      - name: Admin Web validation\n",
            "      - name: Admin Web validation\n        continue-on-error: true\n",
            1,
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("ADMIN_CONTINUE_ON_ERROR:") for issue in issues))

    def test_rejects_removed_or_weakened_admin_commands(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8")

        for command in ci_workflow_audit.EXPECTED_ADMIN_COMMANDS:
            with self.subTest(command=command, change="removed"):
                changed = workflow.replace(f"          {command}\n", "", 1)
                issues = self.audit_text(changed)
                self.assertTrue(any(issue.startswith("ADMIN_COMMAND_DRIFT:") for issue in issues))

            with self.subTest(command=command, change="continue"):
                changed = workflow.replace(
                    f"          {command}\n",
                    f"          {command} || true\n",
                    1,
                )
                issues = self.audit_text(changed)
                self.assertTrue(any(issue.startswith("ADMIN_COMMAND_DRIFT:") for issue in issues))

    def test_rejects_required_job_contents_write(self) -> None:
        workflow = self.with_required_job_permissions("contents: write")

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("JOB_PERMISSION_OVERRIDE:") for issue in issues))

    def test_rejects_required_job_id_token_write(self) -> None:
        workflow = self.with_required_job_permissions("id-token: write")

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("JOB_PERMISSION_OVERRIDE:") for issue in issues))

    def test_rejects_required_job_write_all(self) -> None:
        workflow = self.with_required_job_permissions("write-all")

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("JOB_PERMISSION_OVERRIDE:") for issue in issues))

    def test_rejects_extra_privileged_job(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8") + (
            "\n  privileged:\n"
            "    runs-on: ubuntu-latest\n"
            "    permissions:\n"
            "      security-events: write\n"
            "    steps: []\n"
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("JOB_PERMISSION_OVERRIDE:") for issue in issues))
        self.assertTrue(any(issue.startswith("JOB_SET_DRIFT:") for issue in issues))

    def test_rejects_missing_checkout_credential_policy(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            "        with:\n          persist-credentials: false\n",
            "",
            1,
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("CHECKOUT_CREDENTIAL_PERSISTENCE:") for issue in issues))

    def test_rejects_checkout_credential_persistence(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            "persist-credentials: false",
            "persist-credentials: true",
            1,
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("CHECKOUT_CREDENTIAL_PERSISTENCE:") for issue in issues))

    def test_rejects_named_integration_class_exclusions(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            ci_workflow_audit.CANONICAL_UNIT_SELECTOR,
            "-Dtest='!PostgreSqlHarnessIntegrationTests,"
            "!LearningTransactionIntegrationTests,!OpenApiContractHarnessTests'",
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("UNIT_SELECTOR_DRIFT:") for issue in issues))

    def test_rejects_missing_integration_pattern_exclusion(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            ci_workflow_audit.CANONICAL_UNIT_SELECTOR,
            "-Dtest='!OpenApiContractHarnessTests'",
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("UNIT_SELECTOR_DRIFT:") for issue in issues))

    def test_rejects_unpinned_action(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            "actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1",
            "actions/checkout@v7",
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("UNPINNED_ACTION:") for issue in issues))

    def test_rejects_noncanonical_gate_order(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            "      - name: Unit tests\n",
            "      - name: Unit tests moved\n",
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("GATE_ORDER_DRIFT:") for issue in issues))

    def test_rejects_missing_secret_audit(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            "          python tools/secret_audit.py\n",
            "",
            1,
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("COMMAND_DRIFT:") for issue in issues))

    def test_rejects_missing_secret_audit_tests(self) -> None:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8").replace(
            " tools.test_secret_audit",
            "",
            1,
        )

        issues = self.audit_text(workflow)

        self.assertTrue(any(issue.startswith("COMMAND_DRIFT:") for issue in issues))

    @staticmethod
    def with_required_job_permissions(permission: str) -> str:
        workflow = ci_workflow_audit.WORKFLOW_PATH.read_text(encoding="utf-8")
        if ":" in permission:
            permissions = f"    permissions:\n      {permission}\n"
        else:
            permissions = f"    permissions: {permission}\n"
        return workflow.replace("  required:\n", f"  required:\n{permissions}", 1)

    @staticmethod
    def audit_text(workflow: str) -> list[str]:
        with tempfile.TemporaryDirectory() as temp_directory:
            workflow_path = Path(temp_directory) / "ci.yml"
            workflow_path.write_text(workflow, encoding="utf-8")
            with patch.object(ci_workflow_audit, "WORKFLOW_PATH", workflow_path):
                return ci_workflow_audit.audit()


if __name__ == "__main__":
    unittest.main()
