# simplifyqa-pipeline-executor
This project is a standalone application that is intended to be used in conjunction with SimplifyQA pipelines.<br/>
This connector can work with major CI/CD tools like:
  - Jenkins
  - Concourse CI
  - Gitlab CI
  - Bamboo CI
  - Azure Pipeline
<hr>   

<h1 style="text-align: center;">INTEGRATION WITH JENKINS</h1>

## Steps to configure the pipeline:

<hr>   

<h1 style="text-align: center;">INTEGRATION WITH CONCOURSE CI</h1>

## Command to set the target:
```fly -t sqa-connector login -c http://localhost:8080 -u test -p test```

## Command to check all the targets
```fly targets```

## Command to check previous build(s) (if any)
```fly -t sqa-connector builds```

## Command to set a new pipeline
```fly -t sqa-connector set-pipeline --pipeline {suite_name_or_id} --config set-pipeline.yml --var "GIT_URL=https://{personal_access_token}@github.com/{username}/simplifyqa-connector.git" --var "BRANCH_NAME=main" --var "PAT_SQA_CONNECTOR={personal_access_token}" --var "PSK_SQA_CONNECTOR=" --var "BUILD_API=https://simplifyqa.app/jenkinsSuiteExecution" --var "STATUS_API=https://simplifyqa.app/getJenkinsExecStatus" --var "EXEC_TOKEN={execution_token}"```

## Command to destroy the pipeline
```fly -t sqa-connector destroy-pipeline --pipeline {suite_name_or_id}```

## Command to trigger and watch an execution
```fly -t sqa-connector trigger-job -j {suite_name_or_id}/suite-execution-build-trigger && fly -t sqa-connector watch -j {suite_name_or_id}/suite-execution-build-trigger```

## Command to watch build logs of a specific job with build number
```fly -t sqa-connector watch --job {{suite_name_or_id}}/suite-execution-build-trigger --build NUM```
<hr>   

<h1 style="text-align: center;">INTEGRATION WITH GITLAB CI</h1>

## Steps to configure the pipeline:
```lorem ipsum```
<hr>   

<h1 style="text-align: center;">INTEGRATION WITH BAMBOO CI</h1>

## Steps to configure the pipeline:
```lorem ipsum```
<hr>   

<h1 style="text-align: center;">INTEGRATION WITH AZURE PIPELINES</h1>

## Steps to configure the pipeline:
> ## Step 1: Create a new pipeline
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/56f952d2-50f0-4958-bbb7-26aee6e3e4af)

> ## Step 2: Connect your pipeline to your Github account
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/a2be27c2-a508-44e7-90f0-8476de28ea17)

> ## Step 3: Select the pipeline executor repository
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/9629fb98-5e52-43de-93e1-e73547e5d39d)

> ## Step 4: Review your pipeline configuration
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/46b9e4ed-cb54-49a5-aa18-4e056891d2da)

> ## Step 5: Define the necessary variables required for the pipeline
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/3f7d924a-8691-44f2-9c52-507d12b1666f)

> :warning: **Precautionary Step**
> ## Step 6: Disable automatic triggers as it will trigger a new execution on every git push
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/518e4d72-7535-4791-a118-0abcb52547d9)
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/e0b93ad8-a9e9-4dc7-b9fc-9a5e36efec72)
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/70349de7-93ef-447a-9d62-b392c9d21733)

<hr>

## Steps to Trigger a pipeline execution:
> ## Step 1: Run Pipeline
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/bb17d07c-ecf3-4f49-9993-df57d63d284b)
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/db6f7621-3749-4f20-af1a-2ed18f41f922)

> ## Step 2: Update the value of the execution token as well as other variables if needed
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/68a760f1-bc51-4d85-a688-3b27cbf3249a)
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/cb33a9ae-b2de-455c-8329-8a5fc55f55a9)

> ## Step 3: Jobs will get triggered as mentioned in the pipeline configuration file
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/816a0a7b-95f9-4e57-86d2-83527a912fbe)
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/ad1d884b-1edc-4a25-a98b-aff44165f605)

> ## Step 4: Watch the execution logs using the console window
![image](https://github.com/Simplify3x/simplifyqa-pipeline-executor/assets/78360785/026902d1-f4e8-42fb-bc20-8f9c36b6810b)
![Uploading image.png…]()

