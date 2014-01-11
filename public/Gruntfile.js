module.exports = function (grunt) {
  'use strict';
  grunt.initConfig({
    pkg: grunt.file.readJSON("package.json"),
    bower: {
      install: {
        options: {
          targetDir: './lib',
          layout: 'byType',
          install: true,
          verbose: false,
          cleanTargetDir: true,
          cleanBowerDir: false
        }
      }
    },
    typescript: {
      base: {
        src: ['typescripts/*.ts'],
        dest: 'javascripts/',
        options: {
          module: 'amd', //or commonjs
          target: 'es5', //or es3
          sourcemap: true,
        }
      }
    },
    watch: {
      files: [ 'typescripts/*.ts' ],
      tasks: [ 'compile' ]
    }
  });
  grunt.loadNpmTasks('grunt-bower-task');
  grunt.registerTask('default', ['bower:install']);
  grunt.loadNpmTasks('grunt-typescript');
  grunt.registerTask('compile', ['typescript']);
};
