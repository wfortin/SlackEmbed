var gulp = require('gulp');
var template = require('gulp-template-compile');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');
var path = require('path');

gulp.task('lib', function() {
    return gulp.src([
            'node_modules/socket.io-client/socket.io.js'])
        .pipe(concat('lib.js'))
        .pipe(uglify())
        .pipe(gulp.dest('dist'));
});

gulp.task('templates', function () {
    gulp.src('templates/*.ejs')
        .pipe(template({
            name: function (file) {
                return path.basename(file.relative, path.extname(file.relative));
            }
        }))
        .pipe(concat('templates.js'))
        .pipe(gulp.dest('dist'));
});

gulp.task('watch', function() {
    gulp.watch('templates/**/*.ejs', ['templates']);
});