def configuration
node('android-test') {
    stage('Checkout') {
        // Check out code
        deleteDir()
        checkout scm

        // Load scripts depending on the configuration
        configuration = load "scripts/jenkins/main/develop.groovy"
        checkout = load 'scripts/jenkins/steps/checkOutSource.groovygroovy'
        common = load 'scripts/jenkins/lib/common.groovy'
        gitStatus = load 'scripts/jenkins/lib/git-status.groovy'

        // Post-checkOutSource prep
        checkout.exportGitEnvVars()
        checkout.checkoutBackbone()
        common.stashWorkspace()
        gitStatus.reportGitStatus('Jenkins Job', 'Running job...', 'PENDING')
    }
}
configuration.execute()
