from __future__ import annotations

from pathlib import Path
import re
import subprocess
import sys


REPOSITORY_ROOT = Path(__file__).resolve().parents[1]
PRODUCTION_CONFIG = Path("backend/src/main/resources/application-prod.yml")
ENV_EXAMPLE = Path(".env.example")
GITIGNORE = Path(".gitignore")

PRIVATE_KEY_PATTERN = re.compile(
    r"-----BEGIN (?:[A-Z0-9]+ )*PRIVATE KEY-----"
)
UNQUOTED_CONFIG_ASSIGNMENT_PATTERN = re.compile(
    r"^[ \t]*(?:export[ \t]+)?"
    r"(?P<key>[A-Za-z][A-Za-z0-9_.-]*)[ \t]*[:=][ \t]*"
    r"(?P<value>.*?)[ \t]*(?:#[^\r\n]*)?$",
    re.MULTILINE | re.IGNORECASE,
)
QUOTED_CONFIG_ASSIGNMENT_PATTERN = re.compile(
    r"(?P<quote>['\"])(?P<key>[A-Za-z][A-Za-z0-9_.-]*)(?P=quote)"
    r"[ \t]*:[ \t]*"
    r"(?P<value>\"(?:\\.|[^\"\\])*\"|'(?:\\.|[^'\\])*'|[^,}\r\n]+)",
    re.IGNORECASE,
)
CONFIGURATION_SUFFIXES = {
    ".cfg",
    ".conf",
    ".env",
    ".ini",
    ".json",
    ".log",
    ".properties",
    ".yaml",
    ".yml",
}
FORBIDDEN_SUFFIXES = {
    ".jks",
    ".key",
    ".keystore",
    ".p12",
    ".pem",
    ".pfx",
}
FORBIDDEN_FILENAMES = {
    "credentials.json",
    "service-account.json",
}
REQUIRED_PRODUCTION_BINDINGS = {
    "url: ${SPRING_DATASOURCE_URL}",
    "username: ${SPRING_DATASOURCE_USERNAME}",
    "password: ${SPRING_DATASOURCE_PASSWORD}",
}


def repository_paths(repository_root: Path) -> list[Path]:
    result = subprocess.run(
        [
            "git",
            "ls-files",
            "--cached",
            "--others",
            "--exclude-standard",
            "-z",
        ],
        cwd=repository_root,
        check=True,
        capture_output=True,
    )
    return [Path(item.decode("utf-8")) for item in result.stdout.split(b"\0") if item]


def is_forbidden_tracked_path(path: Path) -> bool:
    normalized = path.as_posix().lower()
    name = path.name.lower()
    if name == ".env" or (name.startswith(".env.") and not name.endswith(".example")):
        return True
    if path.suffix.lower() in FORBIDDEN_SUFFIXES:
        return True
    if name in FORBIDDEN_FILENAMES:
        return True
    return "firebase-adminsdk-" in normalized and normalized.endswith(".json")


def normalized_assignment_value(raw_value: str) -> str:
    value = raw_value.strip()
    if len(value) >= 2 and value[0] == value[-1] and value[0] in {'"', "'"}:
        value = value[1:-1].strip()
    return value


def normalized_sensitive_key(raw_key: str) -> str | None:
    camel_separated = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", raw_key)
    normalized = re.sub(r"[.-]+", "_", camel_separated).lower()
    normalized = re.sub(r"_+", "_", normalized).strip("_")
    compact = re.sub(r"[^a-z0-9]", "", raw_key.lower())
    canonical_exact_keys = {
        "accesskey": "access_key",
        "apikey": "api_key",
        "clientsecret": "client_secret",
        "credential": "credential",
        "credentials": "credentials",
        "password": "password",
        "privatekey": "private_key",
        "secret": "secret",
        "token": "token",
    }
    if compact in canonical_exact_keys:
        return canonical_exact_keys[compact]

    segments = normalized.split("_")

    sensitive = (
        "password" in segments
        or "secret" in segments
        or "token" in segments
        or "credential" in segments
        or "credentials" in segments
        or "apikey" in segments
        or any(
            segments[index : index + 2]
            in (["access", "key"], ["api", "key"], ["private", "key"])
            for index in range(max(0, len(segments) - 1))
        )
    )
    return normalized if sensitive else None


def is_safe_placeholder(value: str) -> bool:
    lowered = value.lower()
    if not value:
        return True
    if lowered in {"false", "null", "true", "~"}:
        return True
    if re.fullmatch(r"\$\{[A-Z][A-Z0-9_]*(?::\?[^}]*)?\}", value):
        return True
    if value.startswith("<") and value.endswith(">"):
        return True
    return any(
        marker in lowered
        for marker in (
            "placeholder",
            "redacted",
            "replace-with-",
            "not-a-real-",
        )
    )


def is_configuration_path(path: Path) -> bool:
    name = path.name.lower()
    return (
        path.suffix.lower() in CONFIGURATION_SUFFIXES
        or name == ".env"
        or name.startswith(".env.")
        or ".env." in name
        or name.endswith(".env")
    )


def configuration_assignments(path: Path, text: str) -> list[tuple[int, str, str]]:
    if not is_configuration_path(path):
        return []

    patterns = [UNQUOTED_CONFIG_ASSIGNMENT_PATTERN]
    if path.suffix.lower() in {".json", ".yaml", ".yml"}:
        patterns.append(QUOTED_CONFIG_ASSIGNMENT_PATTERN)

    assignments: list[tuple[int, str, str]] = []
    for pattern in patterns:
        for match in pattern.finditer(text):
            normalized_key = normalized_sensitive_key(match.group("key"))
            if normalized_key is None:
                continue
            line_number = text.count("\n", 0, match.start()) + 1
            assignments.append((line_number, normalized_key, match.group("value")))
    return assignments


def read_text(path: Path) -> str | None:
    try:
        data = path.read_bytes()
    except OSError:
        return None
    if b"\0" in data:
        return None
    try:
        return data.decode("utf-8")
    except UnicodeDecodeError:
        return None


def audit_paths(repository_root: Path, paths: list[Path]) -> list[str]:
    issues: list[str] = []
    for relative_path in paths:
        if is_forbidden_tracked_path(relative_path):
            issues.append(f"FORBIDDEN_SECRET_FILE:{relative_path.as_posix()}")
            continue

        text = read_text(repository_root / relative_path)
        if text is None:
            continue
        if PRIVATE_KEY_PATTERN.search(text):
            issues.append(f"PRIVATE_KEY_MATERIAL:{relative_path.as_posix()}")
        for line_number, normalized_key, raw_value in configuration_assignments(
            relative_path, text
        ):
            value = normalized_assignment_value(raw_value)
            if not is_safe_placeholder(value):
                issues.append(
                    f"LITERAL_SECRET:{relative_path.as_posix()}:"
                    f"line={line_number}:key={normalized_key}"
                )
    return issues


def audit_configuration(repository_root: Path) -> list[str]:
    issues: list[str] = []
    production_text = read_text(repository_root / PRODUCTION_CONFIG) or ""
    for binding in sorted(REQUIRED_PRODUCTION_BINDINGS):
        if binding not in production_text:
            issues.append(f"PROD_ENV_BINDING_MISSING:{binding}")

    example_text = read_text(repository_root / ENV_EXAMPLE) or ""
    password_match = re.search(r"^POSTGRES_PASSWORD=(.*)$", example_text, re.MULTILINE)
    if password_match is None or not is_safe_placeholder(password_match.group(1).strip()):
        issues.append("ENV_EXAMPLE_PASSWORD_NOT_PLACEHOLDER")

    gitignore_text = read_text(repository_root / GITIGNORE) or ""
    for required_rule in (".env", "!.env.example", "*.key", "*.pem"):
        if required_rule not in gitignore_text.splitlines():
            issues.append(f"GITIGNORE_SECRET_RULE_MISSING:{required_rule}")
    return issues


def audit(repository_root: Path = REPOSITORY_ROOT, paths: list[Path] | None = None) -> list[str]:
    selected_paths = repository_paths(repository_root) if paths is None else paths
    return audit_paths(repository_root, selected_paths) + audit_configuration(repository_root)


def main() -> int:
    issues = audit()
    if issues:
        for issue in issues:
            print(issue)
        print("SECRET_AUDIT=FAIL")
        return 1
    print("SECRET_AUDIT=PASS")
    return 0


if __name__ == "__main__":
    sys.exit(main())
