IF (breaking changes) // major version will be bumped up

    set ThisBuild / versionPolicyIntention := Compatibility.None
    release

ELSE // patch version will be bumped up

    release