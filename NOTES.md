# High Level Design
### Kata UI
* This is a IDE-like web interface that will allow users to complete Katas in a browser
* Users can browse available Katas and submit solutions
* The UI sends the Kata and the user solution to a backend server for execution

### Kata Backend
* This is a web sever which accepts user submissions and executes them
* The backend will also provide an API/interface for administrators to register new Katas into the system
* The backend delegates the actual execution of the Kata to an executor pool

### Kata Executor
* This is a web server that accepts Kata execution requests from the backend
* Each Kata submission is compiled (in the javac sense) and run (in the JUnit runner sense)
* The results of the Kata compilation and execution are sent back to the backend

# Implementation Notes
## Kata Executor
* The Kata executor compiles and executes user code which could be accidentally or intentionally malicious. Examples include
    * Accessing the network ; Taking over the host machine
    * Inifinte loops that eat memory/CPU etc
* Instead of using the Java sandbox, the executor will be sanboxed in a container of some sort; this will deny access to the host machine
* Hogging the CPU is still a problem ; Maybe we need a watchdog to kill runaway executors and/or containers ?
* Executor deployment choices
    * 1 container runs a single executor : executor could be single threaded or multi threaded
    * 1 container runs a several executors : each executor could be single threaded or multi threaded

## Kata Backend
* The backend is relatively simple as all it does is serve Katas to the UI and submits requests to the Executor
* Backend needs to know the address of the executors ; either via static configuration or dynamic discovery
* Kata's can be initially saved to local disk and then moved to some cloud storage 

## Kata UI
* UI can be simple to begin with with basic controls to view sources files and submit the Kata
* We can get some decent interfaces done with CodeMirror/Ace
* Implementing some form or code complete (or canned suggestions) is also pretty cool

# References 
* [Evaluating the Flexibility of the Java Sandbox](https://www.cs.cmu.edu/~clegoues/docs/coker15acsac.pdf)
