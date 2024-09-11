# Sample Maven Application
This repository demonstrates a CI/CD pipeline using GitHub Actions to build, package, and deploy a sample Maven application. The workflows include steps for compiling the code, running tests, building the application, uploading the artifacts to GitHub Packages, and finally deploying the package to a simulated environment.

To kick off a build

# Workflows
## Java CI with Maven
This workflow is responsible for building the Maven application, running tests, and uploading the package to GitHub Packages.

```
name: Java CI with Maven

on:
  push:
    branches:
      - main
  pull_request:
    branches:
      - main

permissions:
  contents: write
  packages: write

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Build with Maven
      run: mvn -B clean install

    - name: Publish to GitHub Packages
      run: mvn deploy
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### Key Steps
- Checkout Code: Retrieves the latest code from the repository.
- Set up JDK: Installs JDK 17, required for building the application.
- Build with Maven: Runs Maven commands to compile and test the application.
- Publish to GitHub Packages: Deploys the built package to GitHub Packages using the default GITHUB_TOKEN.

## Mock Deploy Application
This workflow triggers after the "Java CI with Maven" workflow completes. It downloads the package from GitHub Packages and simulates a deployment to a local directory.

```
name: Mock Deploy Application

on:
  workflow_run:
    workflows: ["Java CI with Maven"]
    types:
      - completed

permissions:
  contents: write
  packages: read

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      with:
        persist-credentials: false
        fetch-depth: 0
        clean: true

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Configure Maven to Use GitHub Packages
      run: |
        mkdir -p $HOME/.m2
        echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      https://maven.apache.org/xsd/settings-1.0.0.xsd">
                <servers>
                  <server>
                    <id>github</id>
                    <username>${{ github.actor }}</username>
                    <password>${{ secrets.ACTIONS_DEMO_TOKEN }}</password>
                  </server>
                </servers>
              </settings>' > $HOME/.m2/settings.xml
      env:
        ACTIONS_DEMO_TOKEN: ${{ secrets.ACTIONS_DEMO_TOKEN }}

    - name: Download Artifact from GitHub Packages
      run: |
        mvn dependency:get -Dartifact=com.example:actions-demo:1.0-SNAPSHOT \
                           -DrepoUrl=https://maven.pkg.github.com/${{ github.repository_owner }}/action-demo \
                           -s $HOME/.m2/settings.xml
      env:
        ACTIONS_DEMO_TOKEN: ${{ secrets.ACTIONS_DEMO_TOKEN }}

    - name: Simulate Deployment
      run: |
        DEPLOY_DIR=$HOME/deployments
        mkdir -p $DEPLOY_DIR
        cp $HOME/.m2/repository/com/example/actions-demo/1.0-SNAPSHOT/actions-demo-1.0-SNAPSHOT.jar $DEPLOY_DIR/
        echo "Deployed actions-demo-1.0-SNAPSHOT.jar to $DEPLOY_DIR"

    - name: Verify Deployment
      run: |
        DEPLOY_DIR=$HOME/deployments
        if [ -f "$DEPLOY_DIR/actions-demo-1.0-SNAPSHOT.jar" ]; then
          echo "Deployment successful!"
        else
          echo "Deployment failed!"
          exit 1
        fi
```

### Key Steps
- Checkout Code: Fetches the latest codebase to access workflow scripts.
- Set up JDK: Installs JDK 17, needed for Maven operations.
- Configure Maven: Sets up Maven with authentication details to access GitHub Packages.
- Download Artifact: Downloads the built package from GitHub Packages.
- Simulate Deployment: Copies the artifact to a simulated deployment directory.
- Verify Deployment: Checks if the deployment was successful by verifying the presence of the artifact in the target directory.

### How to Use
- Fork the Repository: Start by forking this repository to your GitHub account.
- Configure Secrets:
-- Go to your repository's Settings > Secrets and variables > Actions.
-- Add a new secret named ACTIONS_DEMO_TOKEN with a value of your GitHub personal access token (PAT) with read:packages and repo scopes.

### Trigger the Workflows:
- Push changes to the main branch or open a pull request to trigger the "Java CI with Maven" workflow.
- Once the build workflow completes successfully, it will automatically trigger the "Mock Deploy Application" workflow to simulate deployment.

### Requirements
- GitHub Account: A GitHub account with access to create repositories and manage workflows.
- Java 17: The application is built with JDK 17.
- GitHub Packages Permissions: The token used must have appropriate permissions to publish and read packages.
