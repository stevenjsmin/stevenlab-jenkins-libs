// vars/branchChoices.groovy
// 사용법: def branches = branchChoices(repoUrl: 'https://github.com/owner/repo.git')
// - 공개 저장소만 지원 (자격증명 불필요)
// - git 클라이언트가 에이전트에 설치되어 있어야 함

def call(Map cfg = [:]) {
    def repo = cfg.repoUrl
    if (!repo) {
        error "[branchChoices] repoUrl is required"
    }

    // 네트워크/호환성 보장: shallow 없이 heads만 조회
    def raw = sh(returnStdout: true, script: """
        set -euo pipefail
        git ls-remote --heads "${repo}" \
          | awk '{print \$2}' \
          | sed 's|refs/heads/||' \
          | sort -u
    """).trim()

    if (!raw) {
        echo "[branchChoices] No heads found in ${repo}. Fallback to ['main']"
        return ['main']
    }
    return raw.split('\\n') as List<String>
}