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
            sh '''
               if [ "\\`curl -sL -w %{http_code} http://192.168.99.100:9000/api/qualitygates/project_status?projectKey=project_key -o /dev/null -S --quiet 2>&1 | jsawk -a 'return this.status'\\`" == "ERROR" ]; 
               then 
                 exit 1; 
               fi;
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
