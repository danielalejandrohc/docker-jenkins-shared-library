
/*
    This method is to build a docker image
    'credentialsId': This is a credential id of type "User and password"
    'registry': This is docker registry. i.e registry.hub.docker.com
    'registryOrg': This is docker organization name in the registry
    'imageName': This is the name of the image
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
    
    sh "docker push ${args.registry}/${args.registryOrg}/${args.imageName}:${args.dockerTag}";
}