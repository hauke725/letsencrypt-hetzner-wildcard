# letsencrypt-hetzner-wildcard
kotlin native attempt at automatically updating a letsencrypt certificate using the hetzner DNS API. It is still a WIP

## dependencies
letsencrypt

## usage
`./gradlew build` to produce a binary
then run the binary, by default the path is `./build/bin/native/releaseExecutable/letsencrypt-hetzner-wildcard.kexe` and pass the `HETZNER_TOKEN` as an env var. The Hetzner API token can be obtained from the dns console.
The process will ask for a hostname in the form of `*.host.name` - it can also be passed as a parameter
