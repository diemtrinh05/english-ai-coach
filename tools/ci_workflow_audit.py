#!/usr/bin/env python3
from pathlib import Path
import json
import re
import sys

import yaml


ROOT = Path(__file__).resolve().parents[1]
WORKFLOW_PATH = ROOT / ".github" / "workflows" / "ci.yml"
ADMIN_PACKAGE_PATH = ROOT / "admin-web" / "package.json"
ADMIN_WORKING_DIRECTORY = "admin-web"
EXPECTED_NODE_VERSION = "24"
EXPECTED_PACKAGE_MANAGER = "npm@11.19.0"
EXPECTED_ADMIN_COMMANDS = [
    "npm ci",
    "npm run lint",
    "npm run typecheck",
    "npm run test:run",
    "npm run build",
]
EXPECTED_STEP_NAMES = [
    "Checkout",
    "Setup Python",
    "Install audit dependencies",
    "Baseline audit",
    "Setup Java",
    "Setup Node",
    "Build and static checks",
    "Admin Web validation",
    "Unit tests",
    "PostgreSQL integration tests",
    "OpenAPI contract tests",
    "Package",
]
PINNED_ACTION_PATTERN = re.compile(
    r"^actions/(checkout|setup-python|setup-java|setup-node)@[0-9a-f]{40}$"
)
EXPECTED_ACTIONS = {
    "Checkout": "actions/checkout",
    "Setup Python": "actions/setup-python",
    "Setup Java": "actions/setup-java",
    "Setup Node": "actions/setup-node",
}
CANONICAL_UNIT_SELECTOR = "-Dtest='!*IntegrationTests,!OpenApiContractHarnessTests'"
CANONICAL_INTEGRATION_SELECTOR = "-Dtest='*IntegrationTests'"


def audit() -> list[str]:
    issues: list[str] = []

    def require(condition: bool, code: str, detail: str) -> None:
        if not condition:
            issues.append(f"{code}: {detail}")

    require(WORKFLOW_PATH.is_file(), "MISSING_WORKFLOW", str(WORKFLOW_PATH))
    if not WORKFLOW_PATH.is_file():
        return issues

    require(ADMIN_PACKAGE_PATH.is_file(), "MISSING_ADMIN_PACKAGE", str(ADMIN_PACKAGE_PATH))
    if ADMIN_PACKAGE_PATH.is_file():
        try:
            admin_package = json.loads(ADMIN_PACKAGE_PATH.read_text(encoding="utf-8"))
        except json.JSONDecodeError as error:
            issues.append(f"INVALID_ADMIN_PACKAGE: {error}")
        else:
            require(
                admin_package.get("packageManager") == EXPECTED_PACKAGE_MANAGER,
                "ADMIN_PACKAGE_MANAGER_DRIFT",
                f"packageManager phải là {EXPECTED_PACKAGE_MANAGER}",
            )
            engines = admin_package.get("engines", {})
            require(
                isinstance(engines, dict) and engines.get("node") == ">=24 <25",
                "ADMIN_NODE_ENGINE_DRIFT",
                "admin-web phải khóa Node major 24 trong engines.node",
            )
            require(
                isinstance(engines, dict) and engines.get("npm") == ">=11 <12",
                "ADMIN_NPM_ENGINE_DRIFT",
                "admin-web phải khóa npm major 11 trong engines.npm",
            )

    try:
        # BaseLoader giữ khóa `on` là chuỗi thay vì boolean theo YAML 1.1.
        workflow = yaml.load(WORKFLOW_PATH.read_text(encoding="utf-8"), Loader=yaml.BaseLoader)
    except yaml.YAMLError as error:
        return [f"INVALID_YAML: {error}"]

    require(isinstance(workflow, dict), "INVALID_ROOT", "workflow root phải là mapping")
    if not isinstance(workflow, dict):
        return issues

    triggers = workflow.get("on")
    require(isinstance(triggers, dict), "INVALID_TRIGGERS", "`on` phải là mapping")
    if isinstance(triggers, dict):
        require(
            set(triggers) == {"push", "pull_request", "workflow_dispatch"},
            "TRIGGER_DRIFT",
            "chỉ cho phép push, pull_request và workflow_dispatch",
        )
        push = triggers.get("push")
        branches = push.get("branches") if isinstance(push, dict) else None
        require(branches == ["main"], "MAIN_TRIGGER", "push phải chạy trên nhánh main")

    require(
        workflow.get("permissions") == {"contents": "read"},
        "PERMISSION_DRIFT",
        "workflow chỉ được cấp contents: read",
    )

    jobs = workflow.get("jobs")
    require(isinstance(jobs, dict), "INVALID_JOBS", "jobs phải là mapping")
    if not isinstance(jobs, dict):
        return issues

    # Workflow này không cần quyền riêng ở cấp job. Cấm override hoàn toàn để
    # một job hiện tại hoặc được thêm sau không thể nâng quyền top-level.
    for job_id, job in jobs.items():
        require(isinstance(job, dict), "INVALID_JOB", f"job {job_id} phải là mapping")
        if isinstance(job, dict):
            require(
                "permissions" not in job,
                "JOB_PERMISSION_OVERRIDE",
                f"job {job_id} không được override permissions",
            )

    require(set(jobs) == {"required"}, "JOB_SET_DRIFT", "workflow chỉ được có job required")
    required_job = jobs.get("required")
    require(isinstance(required_job, dict), "MISSING_REQUIRED_JOB", "thiếu job required")
    if not isinstance(required_job, dict):
        return issues

    require(required_job.get("runs-on") == "ubuntu-latest", "RUNNER_DRIFT", "runner phải là ubuntu-latest")
    require(required_job.get("timeout-minutes") == "30", "TIMEOUT_DRIFT", "timeout phải là 30 phút")

    steps = required_job.get("steps")
    require(isinstance(steps, list), "INVALID_STEPS", "steps phải là danh sách")
    if not isinstance(steps, list):
        return issues

    names = [step.get("name") if isinstance(step, dict) else None for step in steps]
    require(names == EXPECTED_STEP_NAMES, "GATE_ORDER_DRIFT", "thứ tự gate CI không còn canonical")

    action_steps = [step for step in steps if isinstance(step, dict) and "uses" in step]
    require(len(action_steps) == 4, "ACTION_COUNT", "phải có đúng bốn first-party setup actions")
    for step in action_steps:
        action = step.get("uses", "")
        expected_action = EXPECTED_ACTIONS.get(step.get("name"))
        require(
            bool(PINNED_ACTION_PATTERN.fullmatch(action)),
            "UNPINNED_ACTION",
            f"action phải thuộc allowlist và pin SHA 40 ký tự: {action}",
        )
        require(
            expected_action is not None and action.startswith(f"{expected_action}@"),
            "ACTION_MAPPING_DRIFT",
            f"action không khớp step {step.get('name')}: {action}",
        )

    by_name = {step.get("name"): step for step in steps if isinstance(step, dict)}
    checkout = by_name.get("Checkout", {})
    setup_python = by_name.get("Setup Python", {})
    setup_java = by_name.get("Setup Java", {})
    setup_node = by_name.get("Setup Node", {})
    require(
        checkout.get("with", {}).get("persist-credentials") == "false",
        "CHECKOUT_CREDENTIAL_PERSISTENCE",
        "Checkout phải đặt persist-credentials: false",
    )
    require(setup_python.get("with", {}).get("python-version") == "3.13", "PYTHON_VERSION", "Python phải là 3.13")
    require(setup_java.get("with", {}).get("java-version") == "21", "JAVA_VERSION", "Java phải là 21")
    require(setup_java.get("with", {}).get("distribution") == "temurin", "JAVA_DISTRIBUTION", "JDK phải dùng Temurin")
    require(
        setup_node.get("with", {}).get("node-version") == EXPECTED_NODE_VERSION,
        "NODE_VERSION",
        f"Node phải là major {EXPECTED_NODE_VERSION}",
    )
    require(setup_node.get("with", {}).get("cache") == "npm", "NODE_CACHE", "Setup Node phải cache npm")
    require(
        setup_node.get("with", {}).get("cache-dependency-path") == "admin-web/package-lock.json",
        "NODE_CACHE_DEPENDENCY",
        "Setup Node phải dùng admin-web/package-lock.json làm cache dependency",
    )

    required_commands = {
        "Install audit dependencies": ["--requirement requirements-dev.txt"],
        "Baseline audit": ["python tools/baseline_audit.py"],
        "Build and static checks": [
            "python -m py_compile tools/baseline_audit.py tools/ci_workflow_audit.py tools/secret_audit.py tools/test_ci_workflow_audit.py tools/test_secret_audit.py",
            "python -m unittest tools.test_ci_workflow_audit tools.test_secret_audit",
            "python tools/ci_workflow_audit.py",
            "python tools/secret_audit.py",
            "clean test-compile -DskipTests",
        ],
        "OpenAPI contract tests": ["-Dtest=OpenApiContractHarnessTests test"],
        "Package": ["-DskipTests package"],
    }
    for step_name, fragments in required_commands.items():
        command = by_name.get(step_name, {}).get("run", "")
        for fragment in fragments:
            require(fragment in command, "COMMAND_DRIFT", f"{step_name} thiếu `{fragment}`")

    admin_step = by_name.get("Admin Web validation", {})
    require(
        admin_step.get("working-directory") == ADMIN_WORKING_DIRECTORY,
        "ADMIN_WORKING_DIRECTORY",
        "Admin Web validation phải chạy tường minh trong admin-web",
    )
    require(
        admin_step.get("continue-on-error") is None,
        "ADMIN_CONTINUE_ON_ERROR",
        "Admin Web validation không được phép continue-on-error",
    )
    admin_commands = [
        line.strip()
        for line in admin_step.get("run", "").splitlines()
        if line.strip() and not line.lstrip().startswith("#")
    ]
    require(
        admin_commands == EXPECTED_ADMIN_COMMANDS,
        "ADMIN_COMMAND_DRIFT",
        "Admin Web validation phải chạy đúng npm ci, lint, typecheck, test:run và build",
    )

    unit_command = by_name.get("Unit tests", {}).get("run", "")
    require(
        f"{CANONICAL_UNIT_SELECTOR} test" in unit_command,
        "UNIT_SELECTOR_DRIFT",
        "Unit phải loại toàn bộ *IntegrationTests và OpenApiContractHarnessTests",
    )
    integration_command = by_name.get("PostgreSQL integration tests", {}).get("run", "")
    require(
        f"{CANONICAL_INTEGRATION_SELECTOR} test" in integration_command,
        "INTEGRATION_SELECTOR_DRIFT",
        "PostgreSQL integration gate phải chọn toàn bộ *IntegrationTests",
    )

    return issues


def main() -> int:
    issues = audit()
    if issues:
        print("CI_WORKFLOW_AUDIT=FAIL")
        for issue in issues:
            print(f"- {issue}")
        return 1

    print("CI_WORKFLOW_AUDIT=PASS")
    return 0


if __name__ == "__main__":
    sys.exit(main())
