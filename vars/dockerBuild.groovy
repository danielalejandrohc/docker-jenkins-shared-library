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
        
    usernameP = null;
    passwordP = null;    
    withCredentials([usernamePassword(credentialsId: args.credentialsId, passwordVariable: 'password', usernameVariable: 'username')]) {
        usernameP = username;
        passwordP = password;
    }

    sh "docker login -u ${usernameP} -p ${passwordP} https://${args.registry}";
    echo "Docker login perfomed."
    //sh "docker build -t ${args.registry}/${args.registryOrg}/${args.imageName}:${args.dockerTag} ${args.path}"
}