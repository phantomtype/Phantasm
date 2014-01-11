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

    watch:
      files: [ 'ts/*.ts' ]
      tasks: [ 'compile' ]

  grunt.loadNpmTasks('grunt-bower-task');
  grunt.registerTask('default', ['bower:install']);
  grunt.loadNpmTasks('grunt-typescript');
  grunt.registerTask('compile', ['typescript']);
  grunt.loadNpmTasks('grunt-contrib-watch');
