#!/usr/bin/env bash
echo "Security.sh started"
echo ${TEST_URL}
echo ${SecurityRules}
zap-api-scan.py -t ${TEST_URL}/v2/api-docs -f openapi -u ${SecurityRules} -P 1001 -l FAIL
echo "zap api scan done"
cat zap.out
echo "ZAP has successfully started"
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
zap-cli --zap-url http://0.0.0.0 -p 1001 report -o /zap/api-report.html -f html
mkdir -p functional-output
chmod a+wx functional-output
cp /zap/api-report.html functional-output/
zap-cli --zap-url http://0.0.0.0 -p 1001 alerts -l high --exit-code False