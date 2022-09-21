build:
	javac -cp ".\libs\*" .\src\*.java -d .
run:
	java -cp "./libs/*;./" watch.Main ($ARGS)
