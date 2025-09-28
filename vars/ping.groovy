def call(Map config) {

    echo "-------------------------------------------------------------------------------------------------- ping.groovy"


    // def ARTIFACT_SUB_DIR   = config.ARTIFACT_SUB_DIR
    // def FILE_NAME          = config.FILE_NAME
    
    
    ansiColor('gnome-terminal') {
          timestamps {

              sh """
                  export ANSIBLE_FORCE_COLOR=true
                  export PYTHONUNBUFFERED=1
                  echo "Hi from shell command~~~"
              """
              
          }
    }

    return "Pong~~~";
    
}        
        

