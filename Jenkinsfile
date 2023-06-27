// Builds a module using https://github.com/jenkins-infra/pipeline-library
buildPlugin(
  useContainerAgent: true,
  configurations: [
    [platform: 'linux', jdk: 8],
    [platform: 'windows', jdk: 8],
])
runBenchmarks('jmh-report.json')
