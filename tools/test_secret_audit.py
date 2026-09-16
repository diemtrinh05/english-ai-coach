from collections.abc import Iterator
from contextlib import contextmanager
from contextlib import redirect_stderr
from contextlib import redirect_stdout
import io
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

from tools import secret_audit


class SecretAuditTests(unittest.TestCase):

    def test_canonical_repository_passes(self) -> None:
        self.assertEqual([], secret_audit.audit())

    def test_rejects_literal_secret_assignment(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(root, "config.env.example", f"SERVICE_API_KEY={value}\n")

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn(
                "LITERAL_SECRET:config.env.example:line=1:key=service_api_key",
                issues,
            )

    def test_rejects_lowercase_yaml_sensitive_keys(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(
                root,
                "application-review.yml",
                f"password: {value}\nclient_secret: {value}\napi_key: {value}\n",
            )

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn(
                "LITERAL_SECRET:application-review.yml:line=1:key=password", issues
            )
            self.assertIn(
                "LITERAL_SECRET:application-review.yml:line=2:key=client_secret",
                issues,
            )
            self.assertIn(
                "LITERAL_SECRET:application-review.yml:line=3:key=api_key", issues
            )

    def test_rejects_dotted_properties_password(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(
                root,
                "application-review.properties",
                f"spring.datasource.password={value}\n",
            )

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn(
                "LITERAL_SECRET:application-review.properties:line=1:"
                "key=spring_datasource_password",
                issues,
            )

    def test_rejects_lowercase_env_assignment(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(root, "review.env.example", f"client_secret={value}\n")

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn(
                "LITERAL_SECRET:review.env.example:line=1:key=client_secret", issues
            )

    def test_rejects_json_quoted_sensitive_keys(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(
                root,
                "review.json",
                f'{{"client_secret":"{value}","password":"{value}"}}\n',
            )

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn("LITERAL_SECRET:review.json:line=1:key=client_secret", issues)
            self.assertIn("LITERAL_SECRET:review.json:line=1:key=password", issues)

    def test_sensitive_key_recognition_is_case_insensitive(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(
                root,
                "mixed-case.yaml",
                f"PaSsWoRd: {value}\nclientSecret: {value}\nApiKey: {value}\n",
            )

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn("LITERAL_SECRET:mixed-case.yaml:line=1:key=password", issues)
            self.assertIn(
                "LITERAL_SECRET:mixed-case.yaml:line=2:key=client_secret", issues
            )
            self.assertIn("LITERAL_SECRET:mixed-case.yaml:line=3:key=api_key", issues)

    def test_rejects_yaml_access_key(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(root, "object-storage.yml", f"aws-access-key: {value}\n")

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn(
                "LITERAL_SECRET:object-storage.yml:line=1:key=aws_access_key",
                issues,
            )

    def test_rejects_dotted_properties_access_key(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(
                root,
                "object-storage.properties",
                f"cloud.access-key={value}\n",
            )

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn(
                "LITERAL_SECRET:object-storage.properties:line=1:"
                "key=cloud_access_key",
                issues,
            )

    def test_rejects_aws_access_key_id_env_assignment(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(
                root,
                "object-storage.env.example",
                f"AWS_ACCESS_KEY_ID={value}\n",
            )

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn(
                "LITERAL_SECRET:object-storage.env.example:line=1:"
                "key=aws_access_key_id",
                issues,
            )

    def test_rejects_credential_assignment_key(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(
                root,
                "service.yml",
                f"service-credential: {value}\nservice-credentials: {value}\n",
            )

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn(
                "LITERAL_SECRET:service.yml:line=1:key=service_credential", issues
            )
            self.assertIn(
                "LITERAL_SECRET:service.yml:line=2:key=service_credentials", issues
            )

    def test_rejects_json_camel_case_access_key_and_credential(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(
                root,
                "object-storage.json",
                f'{{"accessKey":"{value}","credential":"{value}"}}\n',
            )

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn(
                "LITERAL_SECRET:object-storage.json:line=1:key=access_key", issues
            )
            self.assertIn(
                "LITERAL_SECRET:object-storage.json:line=1:key=credential", issues
            )

    def test_access_key_and_credential_recognition_is_case_insensitive(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(
                root,
                "mixed-credential.yaml",
                f"ACCESS_KEY: {value}\nServiceCredential: {value}\n",
            )

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn(
                "LITERAL_SECRET:mixed-credential.yaml:line=1:key=access_key", issues
            )
            self.assertIn(
                "LITERAL_SECRET:mixed-credential.yaml:line=2:"
                "key=service_credential",
                issues,
            )

    def test_report_output_redacts_discovered_value(self) -> None:
        with self.secure_fixture() as root:
            value = self.synthetic_secret()
            self.write(root, "redaction.yml", f"service-credential: {value}\n")
            issues = secret_audit.audit(root, self.fixture_paths(root))
            output = io.StringIO()
            error_output = io.StringIO()

            with patch.object(secret_audit, "audit", return_value=issues):
                with redirect_stdout(output), redirect_stderr(error_output):
                    exit_code = secret_audit.main()

            report = output.getvalue()
            self.assertEqual(1, exit_code)
            self.assertIn(
                "LITERAL_SECRET:redaction.yml:line=1:key=service_credential", report
            )
            self.assertNotIn(value, "\n".join(issues))
            self.assertNotIn(value, report)
            self.assertNotIn(value, error_output.getvalue())

    def test_accepts_environment_reference_and_placeholder(self) -> None:
        with self.secure_fixture() as root:
            self.write(
                root,
                "safe.env.example",
                "SERVICE_API_KEY=${SERVICE_API_KEY}\n"
                "SERVICE_PASSWORD=replace-with-a-local-password\n"
                "OBJECT_STORAGE_ACCESS_KEY=${OBJECT_STORAGE_ACCESS_KEY}\n"
                "service-credential=replace-with-a-local-credential\n",
            )

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertEqual([], issues)

    def test_accepts_boolean_credential_control(self) -> None:
        with self.secure_fixture() as root:
            self.write(root, "workflow.yml", "persist-credentials: false\n")

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertEqual([], issues)

    def test_rejects_private_key_material(self) -> None:
        with self.secure_fixture() as root:
            marker = "-----BEGIN " + "PRIVATE KEY-----"
            self.write(root, "notes.txt", f"{marker}\nnot-real-key-data\n")

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn("PRIVATE_KEY_MATERIAL:notes.txt", issues)

    def test_rejects_encrypted_private_key_material(self) -> None:
        with self.secure_fixture() as root:
            marker = "-----BEGIN " + "ENCRYPTED PRIVATE KEY-----"
            self.write(root, "encrypted-notes.txt", f"{marker}\nnot-real-key-data\n")

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn("PRIVATE_KEY_MATERIAL:encrypted-notes.txt", issues)

    def test_rejects_tracked_dotenv(self) -> None:
        with self.secure_fixture() as root:
            self.write(root, ".env", "SAFE=value\n")

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn("FORBIDDEN_SECRET_FILE:.env", issues)

    def test_rejects_credential_file(self) -> None:
        with self.secure_fixture() as root:
            self.write(root, "credentials.json", "{}\n")

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn("FORBIDDEN_SECRET_FILE:credentials.json", issues)

    def test_rejects_missing_production_environment_binding(self) -> None:
        with self.secure_fixture() as root:
            prod_path = root / secret_audit.PRODUCTION_CONFIG
            prod_path.write_text(
                prod_path.read_text(encoding="utf-8").replace(
                    "password: ${SPRING_DATASOURCE_PASSWORD}\n",
                    "",
                ),
                encoding="utf-8",
            )

            issues = secret_audit.audit(root, self.fixture_paths(root))

            self.assertIn(
                "PROD_ENV_BINDING_MISSING:password: ${SPRING_DATASOURCE_PASSWORD}",
                issues,
            )

    @staticmethod
    def write(root: Path, relative_path: str, content: str) -> None:
        path = root / relative_path
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")

    @staticmethod
    def synthetic_secret() -> str:
        return "unit-test-" + "credential"

    @contextmanager
    def secure_fixture(self) -> Iterator[Path]:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            self.write(
                root,
                secret_audit.PRODUCTION_CONFIG.as_posix(),
                "spring:\n"
                "  datasource:\n"
                "    url: ${SPRING_DATASOURCE_URL}\n"
                "    username: ${SPRING_DATASOURCE_USERNAME}\n"
                "    password: ${SPRING_DATASOURCE_PASSWORD}\n",
            )
            self.write(
                root,
                secret_audit.ENV_EXAMPLE.as_posix(),
                "POSTGRES_PASSWORD=replace-with-a-local-password\n",
            )
            self.write(
                root,
                secret_audit.GITIGNORE.as_posix(),
                ".env\n!.env.example\n*.key\n*.pem\n",
            )
            yield root

    @staticmethod
    def fixture_paths(root: Path) -> list[Path]:
        return [path.relative_to(root) for path in root.rglob("*") if path.is_file()]


if __name__ == "__main__":
    unittest.main()
