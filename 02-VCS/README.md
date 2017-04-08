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
Creates new commit from current index.

`branch [-d] <branch-name>`  
Creates a branch with a given name. If `-d` option is specified then branch with a given name is deleted.

`checkout (<commit-hash> | <branch-name>)`

`log`  
Prints a history of commits up until current one.

`merge <branch-name>`

`status`  
Prints a current state of a staging area.

`reset <file>`  
Resets file state in index.

`rm <file>`  
Removes file from file system and updates index.

`clean`  
Removes all untacked files in working directory.