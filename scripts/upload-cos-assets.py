import json
import os
from pathlib import Path

from qcloud_cos import CosConfig, CosS3Client


ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "cos-upload-assets" / "upload-manifest.json"


def required_env(name: str) -> str:
    value = os.environ.get(name)
    if not value:
        raise SystemExit(f"Missing required environment variable: {name}")
    return value


def main() -> None:
    secret_id = required_env("TENCENT_SECRET_ID")
    secret_key = required_env("TENCENT_SECRET_KEY")
    token = os.environ.get("TENCENT_SESSION_TOKEN")
    region = os.environ.get("COS_REGION", "ap-guangzhou")
    bucket = os.environ.get("COS_BUCKET", "ai-file-1409230880")

    client = CosS3Client(CosConfig(
        Region=region,
        SecretId=secret_id,
        SecretKey=secret_key,
        Token=token,
        Scheme="https",
    ))

    assets = json.loads(MANIFEST.read_text(encoding="utf-8"))
    for asset in assets:
        local_path = ROOT / asset["local"]
        key = asset["cosKey"]
        if not local_path.exists():
            raise SystemExit(f"Local file not found: {local_path}")

        client.upload_file(
            Bucket=bucket,
            LocalFilePath=str(local_path),
            Key=key,
            PartSize=1,
            MAXThread=4,
            EnableMD5=False,
        )
        print(f"uploaded {key} -> {asset['publicUrl']}")


if __name__ == "__main__":
    main()
