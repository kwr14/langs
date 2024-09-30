// package cass4io

// 1) Goal Statement
// 2) mechanism: Type class derivation
// 3) why? Improve productivity of Developers

// trait SubscriptionRepo derives RepositoryService {

// }



// read massive Cassandra db and write it to Kafka
// Problem:
    // 1) Cassandra may be down
    // 2) App may crush
    // 3) Kafka may be down
    
    // Solution: make it durable
    // 1) ok to crush but make it resume-able
    

// Source(s)  > > > > > > >  App {Tasks} >>>>>>  Kafka

// Task:  A -> FB, sendToKafka: M => IO[Unit]

// Client -> Server (orchestration (tasks, runs for you durably))

// We need to write a server (durable task execution engine) that can orchestrate tasks.
// the server accepts projects
// project is a set of tasks and pattern of run (chaining, parallel, etc)
// the server keeps track of projects gives them identification and keeps track of the progress of each 
// project
// you can ask the server list all projects, get the status of a project, get the status of a task, etc

// projectId = project-1
 // task, payload, attempt, status, timestamp, projectId

// api to create a project
// api post endpoint to create a project or rerun a project

// def createProject(projectId: String, tasks: List[Task], pattern: Pattern): IO[Unit]

// Hong fu
