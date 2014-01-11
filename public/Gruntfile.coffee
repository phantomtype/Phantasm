
module.exports = (grunt) ->
  pkg = grunt.file.readJSON("package.json")
  grunt.initConfig
    bower:
      install:
        options:
          targetDir: './lib'
          layout: 'byType'
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
          sourcemap: true
          base_path: "ts"

    compass:
      dist:
        options:
          config: 'config.rb'

    watch:
      files: [ 'ts/*.ts' ]
      tasks: [ 'compile' ]

      sass:
        files: [ 'sass/style.scss' ]
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

  grunt.registerTask('default', ['bower:install']);
  grunt.registerTask('compile', ['typescript']);
