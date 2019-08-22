
/*
    This method is to build a docker image
    'registriesConf': This is a configuration mapping to allow to resolve against all these regisrties a single docker build. These allows to combine Docker registries when the 'docker build ...' is performed
    'registry': This is docker registry. i.e registry.hub.docker.com. This is the registry that will be used to perform the tag
    'registryOrg': This is docker organization name in the registry
    'imageName': This is the name of the image
    'dockerFile': (Optional) If not specified the default value will be: 'Dockerfile'
    'path': (Optional) If not specified the default value will be:  '.'
    'dockerTag': (Optional) If not specified the default value will be: 'latest'
*/
def call(args) {
    def MANDATORY_ARGS = ['registriesConf', 'registry', 'registryOrg', 'imageName'];

    // Check for missing (mandatory) parameters
    def given_args = args.keySet();
    MANDATORY_ARGS.each {
        mandatoryArg ->
            if(!args.containsKey(mandatoryArg))
                error "Error: ${mandatoryArg} is missing. Custom step: 'dockerBuild'";
    }

    // Check the structure of the registries configuration
    def mandatoryConfFields = ['credentialId', 'registry'];
    if( args.registriesConf instanceof List ) {
        echo "registriesConf type: Map";          
        
        args.registriesConf.each {
            registryConf ->
                if (registryConf instanceof Map) { 
                    def givenConf = registryConf.keySet();
                    mandatoryConfFields.each {
                        mandatoryArg ->
                            if(!registryConf.containsKey(mandatoryArg)) {
                                error "${mandatoryArg} is missing ${mandatoryArg}"
                            } 
                    }
                } else {
                    error "'registryConf' expected a key-value object (Map) with these parameters ${mandatoryConfFields}"
                }
        }

    } else {
        error "'registriesConf' has incompatible type. It should be 'List' with entries ${mandatoryConfFields}. Type found ${args.registriesConf.getClass()}"
    }

    // If 'path' parameter does not exists then It will assign the default path, which is the current path: '.'
    if(!args.containsKey("path")) {
        args.path = ".";
    }

    if(!args.containsKey("dockerTag")) {
        args.dockerTag = "latest";
    }

    echo "Arguments: ${args}";        

    args.registriesConf.each {
        registryConf -> 
            withCredentials([usernamePassword(credentialsId: registryConf.credentialId, passwordVariable: 'password', usernameVariable: 'username')]) {
                try {
                    sh "docker login -u ${username} -p ${password} https://${registryConf.registry}";
                    echo "Docker login perfomed."
                } catch(e) {
                    error "Error docker login ${e}"
                }
            }
    }    
        
    // Assign default values
    if(!args.containsKey("dockerFile")) {
        // When no 'dockerFile' parameter is provided but 'imageName' is found the it will pull the image from Dockerhub and tag it with the registry provided
        sh "docker pull ${args.imageName}:${args.dockerTag}";
        sh "docker tag ${args.imageName}:${args.dockerTag} ${args.registry}/${args.registryOrg}/${args.imageName}:${args.dockerTag}"
    } else {
        // If the parameter 'dockerFile' is provided then it will build the file
        sh "docker build -f ${args.dockerFile} -t ${args.registry}/${args.registryOrg}/${args.imageName}:${args.dockerTag} -t latest ${args.path}";
    }    
}