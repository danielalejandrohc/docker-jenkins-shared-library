
/*
    This method is to build a docker image
    'credentialsId': This is a credential id of type "User and password"
    'registry': This is docker registry. i.e registry.hub.docker.com
    'registryOrg': This is docker organization name in the registry
    'imageName': This is the name of the image
    'dockerFile': (Optional) If not specified the default value will be: 'Dockerfile'
    'path': (Optional) If not specified the default value will be:  '.'
    'dockerTag': (Optional) If not specified the default value will be: 'latest'
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

    registriesConf = [
        [
            "credentialId": "azureCredentialsId",
            "registry": "acrliq001.azurecr.io"
        ],
        [
            "credentialId": "dockerhubCredentialsId",
            "registry": "registry.hub.docker.com"
        ]
    ]

    // Check If it got a list or single value of Jenkins credentials
    listOfCredentials = [];
    if(args.credentialsId instanceof List) {
        echo "credentialsId type: List";
        listOfCredentials = args.credentialsId;
    } else if (args.credentialsId instanceof String) {
        echo "credentialsId type: String";
        listOfCredentials.add(args.credentialsId);
    } else {
        error "'credentialsId' has incompatible type. It should be List or String. Type found ${args.credentialsId.getClass()}"
    }

    // If 'path' parameter does not exists then It will assign the default path, which is the current path: '.'
    if(!args.containsKey("path")) {
        args.path = ".";
    }

    if(!args.containsKey("dockerTag")) {
        args.dockerTag = "latest";
    }

    echo "Arguments: ${args}";        

    registriesConf.each {
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
        // If parameter 'dockerFile' is provided then it will build the file
        sh "docker build -f ${args.dockerFile} -t ${args.registry}/${args.registryOrg}/${args.imageName}:${args.dockerTag} -t latest ${args.path}";
    }    
}