# simplifyqa-connector
CI/CD connector build for SimplifyQA

# Command to set the target:
fly -t sqa-connector login -c http://localhost:8080 -u test -p test

# Command to check all the targets
fly targets

# Command to check previous build(s) (if any)
fly -t sqa-connector builds

# Command to set a new pipeline
fly -t sqa-connector set-pipeline --pipeline sqa-connector-pipeline --config set-pipeline.yml --var "GIT_URL=https://{personal_access_token}@github.com/bernardbdas/simplifyqa-connector.git" --var "BRANCH_NAME=main" --var "PAT_SQA_CONNECTOR={personal_access_token}" --var "PSK_SQA_CONNECTOR=" --var "BUILD_API=https://simplifyqa.app/jenkinsSuiteExecution" --var "STATUS_API=https://simplifyqa.app/getJenkinsExecStatus" --var "EXEC_TOKEN={execution_token}"

# Command to destroy the pipeline
fly -t sqa-connector destroy-pipeline --pipeline sqa-connector-pipeline

# Command to trigger and watch an execution
fly -t sqa-connector trigger-job -j sqa-connector-pipeline/suite-execution-build-trigger && fly -t sqa-connector watch -j sqa-connector-pipeline/suite-execution-build-trigger
