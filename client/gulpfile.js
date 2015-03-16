var gulp = require('gulp');
var template = require('gulp-template-compile');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');
var typescript = require('gulp-tsc');
var path = require('path');

gulp.task('lib', function() {
    return gulp.src([
            'node_modules/socket.io-client/socket.io.js'])
        .pipe(concat('lib.js'))
        .pipe(uglify())
        .pipe(gulp.dest('dist'));
});

gulp.task('ts', function() {
    return gulp.src('ts/SlackLine.ts')
        .pipe(typescript({
            out: 'app.js',
            outDir: 'dist',
            keepTree: false,
            emitError: false,
            target: 'ES5'
        }))
        .pipe(gulp.dest('dist'));
});

gulp.task('dist', ['lib', 'ts'], function() {
    return gulp.src(['dist/lib.js', 'dist/app.js'])
        .pipe(concat('slackline.js'))
        .pipe(uglify())
        .pipe(gulp.dest('dist'));
});

gulp.task('default', ['dist']);

gulp.task('watch', function() {
    gulp.watch('ts/**/*.ts', ['ts']);
});