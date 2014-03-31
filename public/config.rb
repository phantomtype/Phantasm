# coding: utf-8

# ************************************
# HTTP Path
# ************************************
http_path = "/"

# ************************************
# CSS Directory
# ************************************
css_dir = "css/"

# ************************************
# Sass Directory
# ************************************
sass_dir = "sass/"

# ************************************
# Image Directory
# ************************************
images_dir = "img/"

# ************************************
# JavaScript Directory
# ************************************
javascripts_dir = "js/"

# ************************************
# Other
# ************************************
# .sass-cacheを出力するかどうか
cache = false

# クエストにクエリ文字列付けてキャッシュ防ぐ
asset_cache_buster :none

# Sassファイルをブラウザで確認
sass_options = { :debug_info => false }

# cssの主力形式
output_style = :expanded

# trueで相対パス、falseで絶対パス
relative_assets = true

# CSSファイルにSassファイルの何行目に記述されたものかを出力する
line_comments = false

# # ************************************
# # Sprites
# # ************************************
# # Make a copy of sprites with a name that has no uniqueness of the hash.
# on_sprite_saved do |filename|
#   if File.exists?(filename)
#     FileUtils.cp filename, filename.gsub(%r{-s[a-z0-9]{10}\.png$}, '.png')
#     FileUtils.rm_rf(filename)
#   end
# end
#
# # Replace in stylesheets generated references to sprites
# # by their counterparts without the hash uniqueness.
# on_stylesheet_saved do |filename|
#   if File.exists?(filename)
#     css = File.read filename
#     File.open(filename, 'w+') do |f|
#       f << css.gsub(%r{-s[a-z0-9]{10}\.png}, '.png')
#     end
#   end
# end