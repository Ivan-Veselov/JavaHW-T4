### Inner folder structure

- HEAD
- index
- objects/
    - real files copies (blob)
    - trees
    - commits
- refs/
    - references to commits

### Commands list

`init <folder>`  
Initializes a repository in a given folder.

`add <file>`  
Adds current file state to the index.

`commit <message>`

`branch [-d] <branch-name>`

`checkout (<commit-hash> | <branch-name>)`

`log`

`merge <branch-name>`

`status`  
Prints a current state of a staging area.

`reset <file>`

`rm <file>`

`clean`