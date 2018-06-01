# Dealing with non-root access

The dotwebstack theatre legacy expects to be installed at the root of a webserver (for example: `http://localhost:8080/`. Static content will be fetched from the `/assets` directory (as in our example: `http://localhost:8080/assets`.

Your situation might need a separate starting path, like `http://localhost:8080/mytheatre/`. Two distinct solutions are available:

1. You still want the directory for static content at `/assets`, but all other theatre-specific content should be served from a separate path like `/mytheatre/`
2. You want the static content and the theatre-specific content from the same path (for example `http://localhost:8080/mytheatre/assets` and `http://localhost:8080/mytheatre/...` for all other content).

## 1. Assets from root, other content from specific path
This is the most simple case, and only requires some configuration of the elmo:Stage in you configuration. You should add a `elmo:basePath` statement to your site configuration:

	config:MySite a elmo:Site;
		elmo:domain "localhost"
	.
	config:MyStage a elmo:Stage;
    	elmo:site config:MySite;
    	elmo:basePath "mytheatre"
	.

## 2. Assets and other content from the same specific path

To create this situation, you should following these steps:

(1) Add the correct base path to you site configuration. The base path of the stage will remain empty, for example:

	config:MySite a elmo:Site;
		elmo:domain "localhost";
		elmo:basePath "mytheatre"
	.
	config:MyStage a elmo:Stage;
		elmo:site config:MySite;
		elmo:basePath ""
	.

Static content will be served from `http://localhost:8080/mytheatre/assets`, alle other content will be served from URLs starting with `http://localhost:8080/mytheatre/`.

(2) Check your application.yml file, and make sure it contains something like:

	resources:
	  static-locations: file:./config/static/,classpath:/static/

(3) Copy all files from `\src\main\resources\static\assets` to `\config\static\mytheatre\assets`, where `\config` is the server folder that contains the configuration for your theatre deployment. Replace `mytheatre` the the corresponding name for your situation. Make sure that the location corresponds with the location in application.yml
