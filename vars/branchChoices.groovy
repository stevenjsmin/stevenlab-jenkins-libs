// vars/branchChoices.groovy
// 사용법: def branches = branchChoices(repoUrl: 'https://github.com/owner/repo.git',
//                                      credentialsId: 'my-git-creds')  // 선택

def call(Map cfg = [:]) {
    def repo = cfg.repoUrl
    if (!repo) {
        error "[branchChoices] repoUrl is required"
    }
    def credentialsId = cfg.get('credentialsId', null)

    // 결과를 담을 문자열
    def raw = ''

    if (credentialsId) {
        // HTTPS 자격증명(Username/Password or Token) 사용
        withCredentials([usernamePassword(credentialsId: credentialsId,
                usernameVariable: 'GIT_USER',
                passwordVariable: 'GIT_PASS')]) {
            // https://user:pass@host/path.git 형태로 주입
            def authRepo = repo
            if (repo.startsWith('https://')) {
                authRepo = repo.replace('https://', "https://${GIT_USER}:${GIT_PASS}@")
            }
            raw = sh(returnStdout: true,
                    script: """
                        set -e
                        git ls-remote --heads "${authRepo}" \
                          | awk '{print \$2}' \
                          | sed 's|refs/heads/||' \
                          | sort -u
                     """).trim()
        }
    } else {
        // 공개/토큰불필요 저장소
        raw = sh(returnStdout: true,
                script: """
                    set -e
                    git ls-remote --heads "${repo}" \
                      | awk '{print \$2}' \
                      | sed 's|refs/heads/||' \
                      | sort -u
                 """).trim()
    }

    if (!raw) { return [] }
    return raw.split('\n') as List<String>
}