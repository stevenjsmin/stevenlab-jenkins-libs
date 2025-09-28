def call(Map cfg = [:]) {
    def repo = cfg.repoUrl
    if (!repo) { error "[branchChoices] repoUrl is required" }

    def exec = {
        sh(returnStdout: true, script: """
            set -euo pipefail
            git ls-remote --heads "${repo}" \
              | awk '{print \$2}' \
              | sed 's|refs/heads/||' \
              | sort -u
        """).trim()
    }

    def raw
    if (env.WORKSPACE) {
        // 이미 node/agent 컨텍스트 안
        raw = exec()
    } else {
        // 컨텍스트 없으면 임시로 node 잡아서 실행
        node(cfg.get('label','')) {
            raw = exec()
        }
    }

    if (!raw?.trim()) return ['main']
    return raw.split('\n') as List<String>
}
