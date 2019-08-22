
/*
    This method is to build a docker image
    'credentialsId': 
    'registry': 
    'registryOrg': 
    'imageName':
    'dockerTag':
*/
def call(args) {
    def MANDATORY_ARGS = ['credentialsId', 'registry', 'registryOrg', 'imageName']

    // Check for missing (mandatory) parameters
    def given_args = args.keySet();
    MANDATORY_ARGS.each {
        mandatoryArg ->
            if(!args.containsKey(mandatoryArg))
                error "Error: ${mandatoryArg} is missing. Custom step: 'dockerBuild'"
    }

    // Assign default values
    // If 'dockerfile' parameter does not exists then It will assign the default dockerfile name: 'Dockerfile'
    if(!args.containsKey("dockerFile")) {
        args.dockerFile = "Dockerfile";
    }

    // If 'path' parameter does not exists then It will assign the default path, which is the current path: '.'
    if(!args.containsKey("path")) {
        args.path = ".";
    }

    if(!args.containsKey("dockerTag")) {
        args.dockerTag = "latest";
    }

    echo "Arguments: ${args}";        
           
    withCredentials([usernamePassword(credentialsId: args.credentialsId, passwordVariable: 'password', usernameVariable: 'username')]) {
        try {
            sh "docker login -u ${username} -p ${password} https://${args.registry}";
            echo "Docker login perfomed."
        } catch(e) {
            error "Error docker login ${e}"
        }
    }
    sh "docker push ${args.registry}/${args.registryOrg}/${args.imageName}:${args.dockerTag}"
}