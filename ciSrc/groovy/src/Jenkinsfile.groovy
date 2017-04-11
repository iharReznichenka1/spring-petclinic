node('root') {
   checkout scm
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
         sh '''
            echo "PATH = ${PATH}"
            echo "M2_HOME = ${M2_HOME}"
        '''
      }
      stage('Build') {
         try {
            sh 'mvn -Dmaven.test.failure.ignore=true install'
         } catch (ex) {
            currentBuild.result = 'FAILURE'
            throw ex
         }
      }
      stage('Post') {
         if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
            junit 'target/surefire-reports/**/*.xml'
         }
      }
   }
}
