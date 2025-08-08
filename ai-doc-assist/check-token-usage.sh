#!/bin/bash

# Amazon Q Token Usage Checker
# Verwendung: ./check-token-usage.sh

echo "🔍 Amazon Q Token Usage Report"
echo "================================"

# Aktuelles Datum
TODAY=$(date +%Y-%m-%d)
MONTH_START=$(date +%Y-%m-01)

echo "📅 Zeitraum: $MONTH_START bis $TODAY"
echo ""

# AWS CLI Check
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI nicht installiert. Bitte installieren Sie es zuerst."
    exit 1
fi

# Credentials Check
if ! aws sts get-caller-identity &> /dev/null; then
    echo "❌ AWS Credentials nicht konfiguriert."
    echo "Führen Sie 'aws configure' aus, um Ihre Credentials zu setzen."
    exit 1
fi

echo "✅ AWS CLI konfiguriert"
echo ""

# Aktuelle Kosten abrufen
echo "💰 Aktuelle Kosten für Amazon Q Services:"
aws ce get-cost-and-usage \
  --time-period Start=$MONTH_START,End=$TODAY \
  --granularity MONTHLY \
  --metrics BlendedCost \
  --group-by Type=DIMENSION,Key=SERVICE \
  --filter file://service-filter.json \
  --query 'ResultsByTime[0].Groups[?Keys[0] | contains(@, `Amazon Q`) || contains(@, `Bedrock`)].{Service:Keys[0],Cost:Metrics.BlendedCost.Amount}' \
  --output table

echo ""

# Budget Status (falls Budget existiert)
echo "📊 Budget Status:"
aws budgets describe-budgets \
  --account-id $(aws sts get-caller-identity --query Account --output text) \
  --query 'Budgets[?BudgetName | contains(@, `AI`) || contains(@, `Q`)].{Name:BudgetName,Limit:BudgetLimit.Amount,Unit:BudgetLimit.Unit}' \
  --output table 2>/dev/null || echo "Keine AI/Q-spezifischen Budgets gefunden"

echo ""

# CloudWatch Metriken (falls verfügbar)
echo "📈 CloudWatch Metriken (letzte 24h):"
aws cloudwatch get-metric-statistics \
  --namespace AWS/Bedrock \
  --metric-name Invocations \
  --start-time $(date -u -d '24 hours ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 3600 \
  --statistics Sum \
  --query 'Datapoints[*].{Time:Timestamp,Invocations:Sum}' \
  --output table 2>/dev/null || echo "Keine Bedrock Metriken verfügbar"

echo ""
echo "🔗 Für detaillierte Informationen besuchen Sie:"
echo "   • AWS Console: https://console.aws.amazon.com/billing/"
echo "   • Cost Explorer: https://console.aws.amazon.com/cost-management/home"
echo "   • CloudWatch: https://console.aws.amazon.com/cloudwatch/"
