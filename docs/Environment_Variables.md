# Environment Variables

ConnectedVCS Tools uses environment variables to manage API keys, credentials, and other secrets at runtime. This approach keeps sensitive values out of source code and version control.

## Available Environment Variables

| Variable | Description | Required |
|---|---|---|
| `GOOGLE_MAP_API_KEY` | Google Maps Places Autocomplete API key | Yes |
| `AZURE_MAP_API_KEY` | Azure Maps base map tile API key | Yes |
| `ESRI_MAP_API_KEY` | Esri ArcGIS elevation service API key | Yes |
| `AWS_S3_BUCKET` | S3 bucket name for tile caching | No |
| `AWS_S3_REGION` | AWS region (default: `us-east-1`) | No |
| `AWS_S3_ACCESSKEY` | AWS access key ID | No |
| `AWS_S3_SECRETKEY` | AWS secret access key | No |

Spring Boot automatically maps these environment variables to `application.properties` entries. For example, `GOOGLE_MAP_API_KEY` overrides `google.map.api.key`.

## Usage

### Docker (recommended)

**Option 1: Using a `.env` file** (keeps keys out of shell history)
```bash
# Copy the template and fill in your keys
cp .env.example .env
# Edit .env with your actual values

# Run with env file
docker run -d -p 8080:8080 --env-file .env usdotfhwastol/connectedvcs-tools:<tag>
```

**Option 2: Passing variables directly**
```bash
docker run -d -p 8080:8080 \
  -e GOOGLE_MAP_API_KEY=your-key \
  -e AZURE_MAP_API_KEY=your-key \
  -e ESRI_MAP_API_KEY=your-key \
  usdotfhwastol/connectedvcs-tools:<tag>
```

### Local Development

Export the variables before building and running:
```bash
export GOOGLE_MAP_API_KEY=your-key
export AZURE_MAP_API_KEY=your-key
export ESRI_MAP_API_KEY=your-key
./build.sh
```

Or source from a `.env` file:
```bash
set -a; source .env; set +a
./build.sh
```

## `.env.example` Template

A template file is provided at [`.env.example`](/.env.example) in the repository root. Copy it to `.env` and fill in your values. The `.env` file is gitignored and will not be committed.

## Important

- **Never commit actual API keys or credentials to the repository.** The `application.properties` file contains placeholder values that are overridden by environment variables at runtime.
- Rotate your keys periodically and follow each provider's security best practices.
- For production deployments, consider using a secrets manager (e.g., AWS Secrets Manager, HashiCorp Vault) instead of environment variables.
