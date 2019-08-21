def call(args) {
    def MANDATORY_ARGS = ['credentialsId', 'registry', 'registryOrg', 'imageName']

    //Check for missing (mandatory) parameters
    def given_args = args.keySet();
    def missing_args = MANDATORY_ARGS - given_args;
    if (!missing_args.isEmpty()) {
        error("[CEE Jenkins Shared Library Missing Arguments: ${missing_args}")
    }


    node {
        echo "Hello ${args}"
    }
}