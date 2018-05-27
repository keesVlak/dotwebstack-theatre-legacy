The dotwebstack theatre legacy expects to be installed at the root of a webserver. Static content will be fetched from the `/assets` directory.

In some cases, it is necessary to install the theatre at some path below root, for example at `http://example.org/mytheatre`. Without any change, the theatre will still fetch the static content from `http://example.org/assets`. In most cases, this is not correct, the correct location would be `http://example.org/mytheatre/`.

To create this situation, you should following these steps:

(1) Add the correct base path to you site configuration, for example:

	config:Site a elmo:Site;
    	elmo:domain "localhost";
    	elmo:basePath "mytheatre";
	.

(2) Check your application.yml file, and make sure it contains something like:

	resources:
	  static-locations: file:./config/static/,classpath:/static/

(3) Copy all files from `\src\main\resources\static\assets` to `\config\static\mytheatre\assets`, where `\config` is the server folder that contains the configuration for your theatre deployment. Replace `mytheatre` the the corresponding name for your situation. Make sure that the location corresponds with the location in application.yml