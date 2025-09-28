// vars/branchChoices.groovy
// repoUrl 예: https://github.com/owner/repo.git  (공개 저장소만)

import groovy.json.JsonSlurperClassic

def call(Map cfg = [:]) {
    def repoUrl = cfg.repoUrl
    if (!repoUrl) {
        error "[branchChoices] repoUrl is required"
    }

    // 여기서 슬래시와 콜론을 안전하게 처리
    def m = (repoUrl =~ /github\.com[\/:](.+?)\/([^\/\.]+)(?:\.git)?$/)
    if (!m) {
        error "[branchChoices] Unsupported repoUrl: ${repoUrl}"
    }
    def owner = m[0][1]
    def repo  = m[0][2]
    def api   = "https://api.github.com/repos/${owner}/${repo}/branches?per_page=100"

    def txt = new URL(api).getText(requestProperties: ['User-Agent':'jenkins-branchChoices'])
    def parsed = new JsonSlurperClassic().parseText(txt)
    def names  = parsed.collect { it.name }
    return names ?: ['main']
}
