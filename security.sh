#!/usr/bin/env bash
echo ${TEST_URL}
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
zap-api-scan.py -t ${TEST_URL}/v2/api-docs -f openapi -u ${SecurityRules} -P 1001 -l FAIL
cat zap.out
echo "ZAP has successfully started"
zap-cli --zap-url http://0.0.0.0 -p 1001 report -o /zap/api-report.html -f html
mkdir -p functional-output
chmod a+wx functional-output
cp /zap/api-report.html functional-output/
zap-cli --zap-url http://0.0.0.0 -p 1001 alerts -l High --exit-code False
