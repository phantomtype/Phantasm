
module.exports = (grunt) ->
  pkg = grunt.file.readJSON("package.json")
  grunt.initConfig
    bower:
      install:
        options:
          targetDir: './lib'
          layout: 'byComponent'
          install: true
          verbose: false
          cleanTargetDir: true
          cleanBowerDir: false

    typescript:
      base:
        src: ['ts/*.ts']
        dest: 'js/'
        options:
          module: 'amd'
          target: 'es5'
          sourceMap: true
          basePath: "ts"

    tsd:
      refresh:
        options:
          command: 'reinstall'
          latest: true
          config: './tsd.json'

    compass:
      dist:
        options:
          config: 'config.rb'

    watch:
      ts:
        files: [ 'ts/*.ts' ]
        tasks: [ 'compile' ]

      sass:
        files: [ 'sass/*.scss' ]
        tasks: [ 'compass', 'cmq', 'csscomb' ]
        options:
          nospawn: true

    cmq:
      options:
        log: false
      dev:
        files:
          'css/': ['css/style.css']

    csscomb:
      dev:
        expand: true
        cwd: 'css/'
        src: ['*.css']
        dest: 'css/'

  for taskName of pkg.devDependencies when taskName.substring(0, 6) is 'grunt-'
    grunt.loadNpmTasks taskName

  grunt.registerTask('compile', ['typescript', 'compass', 'cmq', 'csscomb']);
  grunt.registerTask('default', ['bower:install', 'tsd', 'compile']);
