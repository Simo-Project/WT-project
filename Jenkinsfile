pipeline {
  agent any

tools {
  jdk 'JDK-23'
}

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  parameters {
    booleanParam(name: 'RUN_UI_TESTS', defaultValue: false, description: 'Run Selenium UI tests (*SeleniumTest). Requires Chrome/Chromium on the agent.')
    booleanParam(name: 'RUN_SONAR', defaultValue: false, description: 'Run SonarQube analysis (requires Jenkins SonarQube config).')
    string(name: 'SONARQUBE_ENV', defaultValue: 'SonarQube', description: 'Name of SonarQube server in Jenkins (Manage Jenkins → System).')
  }

  environment {
    // Avoids noisy downloads in console
    PATH = "/opt/homebrew/bin:${PATH}"
    MAVEN_ARGS = '-B -ntp'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

   stage('Init') {
     steps {
       script {
         if (isUnix()) {

           if (fileExists('mvnw')) {
             sh 'chmod +x mvnw'
             env.MVN = './mvnw'
           } else {

             env.PATH = "/opt/homebrew/bin:/usr/local/bin:${env.PATH}"

             def mvnPath = sh(returnStdout: true, script: "command -v mvn || true").trim()

             if (!mvnPath) {
               def candidates = [
                 "/opt/homebrew/bin/mvn",
                 "/usr/local/bin/mvn",
                 "/usr/bin/mvn"
               ]
               for (c in candidates) {
                 if (sh(returnStatus: true, script: "test -x '${c}'") == 0) {
                   mvnPath = c
                   break
                 }
               }
             }

             if (!mvnPath) {
               error("""
   mvn not found for Jenkins.
   Fix options:
   1) Recommended: add Maven Wrapper to the repo (mvn -N wrapper:wrapper, commit mvnw + .mvn)
   2) Install Maven on the Jenkins agent and make sure Jenkins can see it (brew install maven)
   Current PATH: ${env.PATH}
   """)
             }

             env.MVN = mvnPath
           }
         } else {
           env.MVN = fileExists('mvnw.cmd') ? 'mvnw.cmd' : 'mvn'
         }

         echo "Using Maven command: ${env.MVN}"
         if (isUnix()) {
           sh "${env.MVN} -v"
         } else {
           bat "${env.MVN} -v"
         }
       }
     }
   }

    stage('Build + Test (non-UI)') {
      steps {
        script {
          // Exclude Selenium tests by default so CI doesn't fail without Chrome.
          // Note: -Dtest excludes apply to Surefire; -Dit.test applies to Failsafe.
          def excludeUi = "-Dtest='!*SeleniumTest' -Dit.test='!*SeleniumTest'"

          if (isUnix()) {
            sh "${env.MVN} ${env.MAVEN_ARGS} clean verify ${excludeUi}"
          } else {
            bat "${env.MVN} ${env.MAVEN_ARGS} clean verify ${excludeUi}"
          }
        }
      }
    }

    stage('UI Tests (Selenium)') {
      when { expression { return params.RUN_UI_TESTS } }
      steps {
        script {

          def chromeBin = ''

          if (isUnix()) {

            def candidates = [
              "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
              "${env.HOME}/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
              "/usr/bin/google-chrome",
              "/usr/bin/google-chrome-stable",
              "/usr/bin/chromium",
              "/usr/bin/chromium-browser"
            ]
            for (c in candidates) {
              def status = sh(returnStatus: true, script: "test -x '${c}'")
              if (status == 0) { chromeBin = c; break }
            }
          } else {

            def candidates = [
            "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
              "C:\\\\Program Files\\\\Google\\\\Chrome\\\\Application\\\\chrome.exe",
              "C:\\\\Program Files (x86)\\\\Google\\\\Chrome\\\\Application\\\\chrome.exe"
            ]

            chromeBin = candidates[0]
          }

          if (isUnix() && !chromeBin) {
            error("""RUN_UI_TESTS=true but Chrome/Chromium wasn't found on this agent.
Install Chrome, or adjust the path list in Jenkinsfile, or run UI tests in a Selenium Docker container.""")
          }

          echo "Using Chrome binary: ${chromeBin}"

          def chromeProp = isUnix() ? "-Dwebdriver.chrome.bin='${chromeBin}'" : "-Dwebdriver.chrome.bin=\"${chromeBin}\""

          if (isUnix()) {
            sh "${env.MVN} ${env.MAVEN_ARGS} -Dtest='*SeleniumTest' -DfailIfNoTests=false test ${chromeProp}"
          } else {
            bat "${env.MVN} ${env.MAVEN_ARGS} -Dtest=*SeleniumTest -DfailIfNoTests=false test ${chromeProp}"
          }
        }
      }
    }

    stage('SonarQube Analysis') {
      when { expression { return params.RUN_SONAR } }
      steps {
        script {

          withSonarQubeEnv(params.SONARQUBE_ENV) {
            if (isUnix()) {
              sh "${env.MVN} ${env.MAVEN_ARGS} sonar:sonar"
            } else {
              bat "${env.MVN} ${env.MAVEN_ARGS} sonar:sonar"
            }
          }
        }
      }
    }
  }

  post {
    always {

      junit testResults: '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml',
            allowEmptyResults: true

      jacoco execPattern: '**/target/*.exec',
             classPattern: '**/target/classes',
             sourcePattern: '**/src/main/java'

      archiveArtifacts artifacts: 'target/site/jacoco/**/*', allowEmptyArchive: true
    }
  }
}