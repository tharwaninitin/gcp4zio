## STEP 1 => Update README.md using mdoc
```shell
sbt docs/mdoc
```

## STEP 2 => Merge PR into main

## STEP 3 => Create tag(x.x.x) in main branch locally for the latest version and update remotely (use GitHub Desktop)

## STEP 4 => Follow below release process locally from the main branch
IF (breaking changes) // Bump up major version using below settings
```
set ThisBuild / versionPolicyIntention := Compatibility.None
release
```
ELSE // patch version will be bumped up
```
release
```

## STEP 5 => Create release using the above tag (Using github.com UI)

## STEP 6 => Create new branch (vnext) from main with version(+1) changes