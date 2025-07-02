# 🚀 AI Brother Model Distribution Infrastructure Setup

This guide provides complete instructions for setting up a robust, scalable model distribution system for AI Brother.

## 📋 Overview

The model distribution system consists of:

1. **Model Catalog Service** - JSON-based catalog with model metadata
2. **CDN Network** - Multiple geographic mirrors for fast downloads
3. **Integrity Verification** - SHA256 checksums for file validation
4. **Fallback System** - Multiple download sources for reliability
5. **Analytics** - Download tracking and usage metrics

## 🏗️ Infrastructure Components

### 1. Primary CDN (Cloudflare R2 + CDN)

**Why Cloudflare R2:**
- Global edge network for fast downloads
- No egress fees for downloads
- Built-in DDoS protection
- Easy integration with Cloudflare CDN

**Setup Steps:**

```bash
# 1. Create R2 bucket
wrangler r2 bucket create aibrother-models

# 2. Configure CORS
wrangler r2 bucket cors put aibrother-models --file cors.json
```

**cors.json:**
```json
[
  {
    "AllowedOrigins": ["*"],
    "AllowedMethods": ["GET", "HEAD"],
    "AllowedHeaders": ["*"],
    "MaxAgeSeconds": 3600
  }
]
```

**Upload models:**
```bash
# Upload model files with metadata
wrangler r2 object put aibrother-models/phi-2-mobile-q4_k_m.gguf --file phi-2-mobile-q4_k_m.gguf
wrangler r2 object put aibrother-models/catalog.json --file model-catalog.json
```

### 2. AWS S3 Backup (Multi-Region)

**US East (Primary):**
```bash
# Create S3 bucket
aws s3 mb s3://aibrother-models --region us-east-1

# Configure public read access
aws s3api put-bucket-policy --bucket aibrother-models --policy file://bucket-policy.json

# Upload models
aws s3 sync ./models/ s3://aibrother-models/ --exclude "*" --include "*.gguf"
aws s3 cp model-catalog.json s3://aibrother-models/catalog.json
```

**EU West (Mirror):**
```bash
# Create EU mirror
aws s3 mb s3://aibrother-models-eu --region eu-west-1

# Set up cross-region replication
aws s3api put-bucket-replication --bucket aibrother-models --replication-configuration file://replication.json
```

**bucket-policy.json:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::aibrother-models/*"
    }
  ]
}
```

### 3. GitHub Releases (Fallback)

**Setup automated releases:**

```yaml
# .github/workflows/model-release.yml
name: Model Release
on:
  workflow_dispatch:
    inputs:
      model_name:
        description: 'Model to release'
        required: true

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Create Release
        uses: softprops/action-gh-release@v1
        with:
          tag_name: models-v${{ github.run_number }}
          name: AI Models Release
          files: |
            models/*.gguf
            model-catalog.json
```

### 4. Model Catalog Hosting

**Multiple endpoints for high availability:**

1. **Primary:** `https://cdn.aibrother.app/models/catalog.json`
2. **AWS S3:** `https://aibrother-models.s3.amazonaws.com/catalog.json` 
3. **GitHub:** `https://raw.githubusercontent.com/aibrother/model-catalog/main/catalog.json`
4. **Cloudflare:** `https://models.aibrother.app/catalog.json`

## 🔧 Model Processing Pipeline

### 1. Model Preparation Script

```bash
#!/bin/bash
# prepare-model.sh

MODEL_NAME=$1
SOURCE_URL=$2
OUTPUT_DIR="./models"

echo "🔄 Processing model: $MODEL_NAME"

# Download original model
wget -O "${OUTPUT_DIR}/${MODEL_NAME}.original" "$SOURCE_URL"

# Convert to GGUF if needed (using llama.cpp)
if [[ "$SOURCE_URL" != *.gguf ]]; then
    python convert.py "${OUTPUT_DIR}/${MODEL_NAME}.original" \
        --outfile "${OUTPUT_DIR}/${MODEL_NAME}.gguf" \
        --outtype q4_k_m
fi

# Calculate SHA256
SHA256=$(sha256sum "${OUTPUT_DIR}/${MODEL_NAME}.gguf" | cut -d' ' -f1)
echo "✅ SHA256: $SHA256"

# Generate model metadata
cat > "${OUTPUT_DIR}/${MODEL_NAME}.json" << EOF
{
  "filename": "${MODEL_NAME}.gguf",
  "sha256": "$SHA256",
  "size_bytes": $(stat -c%s "${OUTPUT_DIR}/${MODEL_NAME}.gguf"),
  "processed_date": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

echo "✅ Model prepared: ${MODEL_NAME}"
```

### 2. Automated Upload Script

```bash
#!/bin/bash
# upload-models.sh

MODELS_DIR="./models"
CATALOG_FILE="model-catalog.json"

echo "🚀 Uploading models to all mirrors..."

for model in ${MODELS_DIR}/*.gguf; do
    filename=$(basename "$model")
    echo "📤 Uploading $filename..."
    
    # Upload to Cloudflare R2
    wrangler r2 object put aibrother-models/"$filename" --file "$model"
    
    # Upload to AWS S3 (US)
    aws s3 cp "$model" s3://aibrother-models/"$filename"
    
    # Upload to AWS S3 (EU) 
    aws s3 cp "$model" s3://aibrother-models-eu/"$filename"
    
    echo "✅ $filename uploaded to all mirrors"
done

# Upload catalog to all endpoints
echo "📋 Uploading catalog..."
wrangler r2 object put aibrother-models/catalog.json --file "$CATALOG_FILE"
aws s3 cp "$CATALOG_FILE" s3://aibrother-models/catalog.json
aws s3 cp "$CATALOG_FILE" s3://aibrother-models-eu/catalog.json

echo "🎉 All uploads complete!"
```

## 📊 Analytics Setup

### 1. Download Tracking

**Cloudflare Analytics Dashboard:**
```javascript
// Custom analytics worker
export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    
    // Track download requests
    if (url.pathname.endsWith('.gguf')) {
      await env.ANALYTICS.writeDataPoint({
        'blobs': [url.pathname],
        'doubles': [1],
        'indexes': [url.pathname.split('/').pop()]
      });
    }
    
    return fetch(request);
  }
};
```

**AWS CloudWatch Metrics:**
```bash
# Enable S3 request metrics
aws s3api put-bucket-metrics-configuration \
    --bucket aibrother-models \
    --id EntireBucket \
    --metrics-configuration Id=EntireBucket,Filter='{}'
```

### 2. Usage Dashboard

**Grafana Dashboard Configuration:**
```json
{
  "dashboard": {
    "title": "AI Brother Model Distribution",
    "panels": [
      {
        "title": "Downloads by Model",
        "type": "stat",
        "targets": [
          {
            "query": "cloudflare_requests_total{path=~\".*\\.gguf\"}"
          }
        ]
      },
      {
        "title": "Geographic Distribution", 
        "type": "worldmap",
        "targets": [
          {
            "query": "cloudflare_requests_by_country"
          }
        ]
      }
    ]
  }
}
```

## 🔒 Security & Access Control

### 1. API Rate Limiting

**Cloudflare Rate Limiting:**
```bash
# Limit downloads to prevent abuse
curl -X POST "https://api.cloudflare.com/client/v4/zones/{zone_id}/rate_limits" \
     -H "Authorization: Bearer {api_token}" \
     -H "Content-Type: application/json" \
     --data '{
       "threshold": 100,
       "period": 3600,
       "action": {
         "mode": "simulate"
       },
       "match": {
         "request": {
           "url": "*.aibrother.app/models/*.gguf"
         }
       }
     }'
```

### 2. Access Logs

**Enable comprehensive logging:**
```bash
# AWS S3 access logging
aws s3api put-bucket-logging \
    --bucket aibrother-models \
    --bucket-logging-status file://logging.json

# Cloudflare logpush
curl -X POST "https://api.cloudflare.com/client/v4/zones/{zone_id}/logpush/jobs" \
     -H "Authorization: Bearer {api_token}" \
     -d '{"destination_conf":"s3://aibrother-logs?region=us-east-1"}'
```

## 🚀 Deployment Checklist

### Pre-Launch
- [ ] All models uploaded to primary CDN
- [ ] Mirror synchronization verified
- [ ] Catalog JSON validated and deployed
- [ ] SHA256 checksums verified for all models
- [ ] Download speeds tested from multiple regions
- [ ] Fallback URLs tested and working
- [ ] Rate limiting configured
- [ ] Analytics dashboards set up

### Launch
- [ ] Update app catalog URLs to production
- [ ] Monitor download success rates
- [ ] Track geographic distribution
- [ ] Monitor CDN performance metrics
- [ ] Set up alerting for failures

### Post-Launch
- [ ] Establish model update process
- [ ] Set up automated testing
- [ ] Create model submission pipeline
- [ ] Document maintenance procedures

## 💰 Cost Estimation

### Monthly Costs (Estimated)

| Service | Cost | Notes |
|---------|------|-------|
| Cloudflare R2 | $15-50 | Based on storage + requests |
| AWS S3 (US) | $20-100 | 100GB storage + transfer |
| AWS S3 (EU) | $25-120 | EU pricing slightly higher |
| Cloudflare CDN | $0-20 | Free tier generous |
| Analytics | $10-30 | Grafana Cloud or similar |
| **Total** | **$70-320/month** | Scales with usage |

### Optimization Tips

1. **Use CloudFlare R2** as primary (no egress fees)
2. **Enable compression** where possible
3. **Implement smart caching** (models rarely change)
4. **Use lifecycle policies** for old model versions
5. **Monitor and optimize** based on usage patterns

## 🔄 Maintenance & Updates

### Regular Tasks

**Weekly:**
- Review download analytics
- Check mirror synchronization
- Monitor error rates

**Monthly:**
- Update model catalog
- Review costs and optimization
- Test failover scenarios

**Quarterly:**
- Major infrastructure updates
- Security audits
- Performance benchmarking

### Model Update Process

1. **Prepare new model** using processing pipeline
2. **Upload to staging** environment first
3. **Validate integrity** and metadata
4. **Deploy to production** mirrors
5. **Update catalog** with new model info
6. **Announce** to community

## 📞 Support & Monitoring

### Alerting Setup

```yaml
# alerts.yml
groups:
  - name: model_distribution
    rules:
      - alert: HighDownloadFailureRate
        expr: (download_failures / download_attempts) > 0.05
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High download failure rate detected"
          
      - alert: CDNDown
        expr: up{job="cdn_health_check"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Primary CDN is down"
```

### Health Check Endpoints

```bash
# Simple health check script
#!/bin/bash
for url in "${CATALOG_URLS[@]}"; do
    if curl -f "$url" > /dev/null 2>&1; then
        echo "✅ $url is healthy"
    else
        echo "❌ $url is down"
        # Send alert
    fi
done
```

---

This infrastructure setup provides a robust, scalable, and cost-effective model distribution system that can handle thousands of concurrent downloads while maintaining high availability and performance worldwide.