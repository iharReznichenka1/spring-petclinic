node('root') {
   String toolJdk = tool name: "jdk8", type: 'hudson.model.JDK'
   def mvnHome = tool name: 'maven 3.5'

   /* Set JAVA_HOME, and special PATH variables. */
   List javaEnv = [
           "PATH+MVN=${toolJdk}/bin:${mvnHome}/bin",
           "M2_HOME=${mvnHome}",
           "JAVA_HOME=${toolJdk}"
   ]

   withEnv(javaEnv) {
      stage('Initialize') {
         checkout scm
         sh '''
               echo "PATH = ${PATH}"
               echo "M2_HOME = ${M2_HOME}"
            '''
      }
      try {
         stage('Build') {
            sh 'mvn -Dmaven.test.failure.ignore=true install'
         }
         stage('SonarQube analysis') {
            withSonarQubeEnv('My SonarQube Server') {
               sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.3.0.603:sonar'
            }
            sleep 10
            sh '''
                  url=`cat $WORKSPACE/target/sonar/report-task.txt |grep ceTaskUrl |cut -f2,3 -d “=”`
                  status=`curl -u admin:admin $url |jq .task.status|sed -e ‘s/^”//’  -e ‘s/”$//’`
                  if [[ “$status”==”SUCCESS” ]]; then
                  analysisID=`curl -u admin:admin $url |jq ‘.task.analysisId’|sed -e ‘s/^”//’  -e ‘s/”$//’`
                  else
                  echo “SonarQube run was not success”
                  exit 1;
                  fi
                  analysisUrl=”http://localhost:9000/api/qualitygates/project_status?analysisId=&#8221;
                  quality_gate_status=`curl -u admin:admin $analysisUrl${analysisID} |jq ‘.projectStatus.status’|sed -e ‘s/^”//’  -e ‘s/”$//’`
                  if [[ “$quality_gate_status” != “OK” ]]; then
                  echo “Error at quality gate validation”
                  exit 1;
                  fi
                '''
         }
      } catch (ex) {
         currentBuild.result = 'FAILURE'
         mail to: 'ihar.reznichenka@gmail.com', subject: "Failed Pipeline: ${currentBuild.fullDisplayName}", body: "Something is wrong with ${env.BUILD_URL}"
         throw ex
      }
      stage('Summary') {
         if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
            junit 'target/surefire-reports/**/*.xml'
         }
      }
   }
}
