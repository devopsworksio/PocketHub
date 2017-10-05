import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import hudson.model.*


config = [
        fastDexguardBuilds: false
]


def getGitHubSHA(changeId) {
    try {
        withCredentials([[$class: 'StringBinding', credentialsId: 'github', variable: 'GITHUB_TOKEN']]) {

            def apiUrl = "https://api.github.com/repos/devopsworksio/PocketHub/pulls/${changeId}"
            def response = sh(returnStdout: true, script: "curl -s -H \"Authorization: Token ${env.GITHUB_TOKEN}\" -H \"Accept: application/json\" -H \"Content-type: application/json\" -X GET ${apiUrl}").trim()
            echo "${response}"
            def jsonSlurper = new JsonSlurper()
            def data = jsonSlurper.parseText("${response}")
            return data.head['sha']
        }
    } catch (error) {
        echo "${error}"

        error("Failed to get GitHub SHA for PR")
    }
}



def prepareWorkspace() {
    deleteDir()
    unstash 'sources'
    unstash 'backbone-babylon'
    sh 'mv dist-babylon.zip app/src/main/assets/dist-babylon.zip'
    sh '''
        cd app/src/main/assets
        unzip dist-babylon.zip
    '''
}

def printDaemonStatus() {
    sh './gradlew --status'
}

def stashWorkspace() {
    stash(name: 'sources', excludes: 'backbone/**,**/dist/**')
}

def hockeyUpload(String apkName, String appId) {
    def keys = load 'scripts/jenkins/lib/keys.groovy'
    withCredentials(keys.hockeyUploadKey) {
        step([$class: 'HockeyappRecorder', applications: [[apiToken: env.HOCKEY_API_TOKEN, downloadAllowed: true, filePath: apkName, mandatory: false, notifyTeam: false, releaseNotesMethod: [$class: 'ChangelogReleaseNotes'], uploadMethod: [$class: 'VersionCreation', appId: appId]]], debugMode: true, failGracefully: false])
    }
}

def slackFeed() {
    String color;
    String result;
    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
        color = 'good'
        result = 'successful'
    } else if (currentBuild.result == 'UNSTABLE') {
        color = 'warning'
        result = 'unstable'
    } else {
        color = 'danger'
        result = 'failed!'
    }

    try {
        withCredentials([[$class: 'StringBinding', credentialsId: 'ANDROID_SLACK_INTEGRATION_KEY', variable: 'ANDROID_SLACK_INTEGRATION_KEY']]) {
            slackSend channel: 'android_feed', color: color, message: "Build ${result} - ${env.BRANCH_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", teamDomain: 'babylonhealth', token: env.ANDROID_SLACK_INTEGRATION_KEY
        }
    } catch (error) {
        // this is not fatal just annoying
        echo ">>> Slack feed updated failed! <<<"
    }
}

def reportFinalBuildStatus() {

        unstash 'pipeline'
        def gitStatus = load 'scripts/jenkins/lib/git-status.groovy'
        def body = """
        Build Succeeded!...
        Build Number: ${env.BUILD_NUMBER}
        Jenkins URL: ${env.BUILD_URL}
        Git Commit: ${env.GIT_COMMIT}
       """
        echo "Job result : ${currentBuild.result}"
        if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
            gitStatus.reportGitStatus('Jenkins Job', 'Job successful!', 'SUCCESS')

            common.notifyJira(body, env.JIRA_ISSUE)
        } else {
            gitStatus.reportGitStatus('Jenkins Job', 'Job failed!', 'FAILURE')
            common.notifyJira("Build Failed!", env.JIRA_ISSUE)
        }

}


def notifyJira(String message, String key) {
    if (key != 'None' || key != null) {
        try {
            jiraComment body: message, issueKey: key
        } catch (error) {
            echo ">>> JIRA Notification failed! ${error} <<<"
        }
    }
}

def gradleParameters() {
    "-PcustomVersionCode=${env.BUILD_COUNTER} -PjenkinsFastDexguardBuildsEnabled=${config.fastDexguardBuilds} -Dorg.gradle.java.home=${env.JAVA_HOME} -Pandroid.enableBuildCache=false -PtestCoverageFlag=true --profile --no-daemon"
}

def archiveCommonArtifacts() {
    archive '**/*mapping.txt,**/reports/**'
}

def archiveGradleCrashLogs() {
    archive 'hs_err_*,**/hs_err_*'
}

@NonCPS
def buildCounter() {
    build 'build-counter'
    def job = Jenkins.instance.getItemByFullName('build-counter')
    def item = job.getLastSuccessfulBuild().number
    return item

}

return this
