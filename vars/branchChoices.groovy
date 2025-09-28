// vars/branchChoices.groovy
// 공개 GitHub 저장소 전용. credentialsId 없이 동작.
// repoUrl 예: https://github.com/owner/repo.git 또는 https://github.com/owner/repo

import groovy.json.JsonSlurperClassic

def call(Map cfg = [:]) {
    def repoUrl = cfg.repoUrl
    if (!repoUrl) {
        error "[branchChoices] repoUrl is required"
    }

    // owner/repo 추출
    def m = (repoUrl =~ /github\.com[/:]([^/]+)\/([^/.]+)(?:\.git)?$/)
    if (!m) {
        error "[branchChoices] Unsupported repoUrl: ${repoUrl}"
    }
    def owner = m[0][1]
    def repo  = m[0][2]
    def api   = "https://api.github.com/repos/${owner}/${repo}/branches?per_page=100"

            // 1) httpRequest 플러그인이 있으면 사용 (권장)
    if (stepsAvailable('httpRequest')) {
        def resp = httpRequest(
                url: api,
                httpMode: 'GET',
                validResponseCodes: '200',
                customHeaders: [[name: 'User-Agent', value: 'jenkins-branchChoices']]
        )
        return parseBranches(resp.getContent())
    }

    // 2) 플러그인 없으면 순수 Groovy URL로 fallback
    def txt = fetchViaURL(api)
    return parseBranches(txt)
}

// ---- helpers ----

@NonCPS
private List<String> parseBranches(String jsonText) {
    if (!jsonText?.trim()) return ['main']
    def parsed = new JsonSlurperClassic().parseText(jsonText)
    def names = (parsed instanceof List) ? parsed.collect { it.name as String } : []
    if (!names) return ['main']
    return names.unique().sort()
}

@NonCPS
private String fetchViaURL(String urlStr) {
    def conn = new URL(urlStr).openConnection()
    conn.setRequestProperty('User-Agent', 'jenkins-branchChoices')
    conn.setConnectTimeout(10000)
    conn.setReadTimeout(15000)
    conn.doInput = true
    return conn.inputStream.getText('UTF-8')
}

@NonCPS
private boolean stepsAvailable(String stepName) {
    try {
        // 라이브러리에서 파이프라인 스텝 존재 여부 점검
        def dsl = org.jenkinsci.plugins.workflow.cps.DSL.getThreadCurrentDSL()
        dsl.invokeMethod(stepName, [[$class: 'org.jenkinsci.plugins.workflow.steps.EchoStep', message: 'probe']])
        return true
    } catch (Throwable ignore) {
        return false
    }
}
