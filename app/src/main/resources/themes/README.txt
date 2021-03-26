THE BASICS OF THEMES
0. Welcome
    This guide will teach you the basics of themes.

    1. Create a theme
    2. Keywords
        2.1 Language Keywords
        2.2 Explanation of Keywords
    3. Warnings and Debugging
    4. List of Supported Keys

-------------------------------------------------------------------------------------

1. Create a theme
    To create a theme, simply create a new file with '.theme' as the file extension.

2. Keywords
    2.1 Language Keywords
    A theme currently has 3 language keywords covering the following:
        * Theme name
        * Color
        * Comment

    2.2 Explanation of Keywords
    * Name
        The name of the theme. It can consist of the letters A-Z (both upper and lowercase) and spaces.
        Syntax: name = "<name>";

    * Color
        A color which is a key value pair consisting of a name and a color in RGB format (values from 0 to 255).
        Syntax: "<key>" = [<red>, <green>, <blue>];

    * Comment
        A comment which is ignored at load time. Good for making things clear.
        Syntax: #<text>

3. Warnings and Debugging
    You can get the following Warnings if doing something wrong.
        * "No name is set for theme file '<fileName>'! -> setting to 'Unknown'."
            Explanation: The theme name keyword is missing or the syntax is wrong.
            Fix: Use the correct syntax -> name = "<name>" or add the line of text.

        * "Theme '<name>' is empty! All colors will be the same."
            Explanation: The theme color palette is empty. There are no Color keys present in the theme file.
            Fix: Please add some colors using the syntax: <key> = [<red>, <green>, <blue>].

        * Wrong syntax at: '<keyword>' (line: <lineNumber>)
            Explanation: The syntax of a line is wrong. Please correct the line according to the correct syntax.
            Fix: Use the printed linenumber to debug the faulty code.

        * No matching color to key: '<key>'. Returning BLACK!
            Explanation: A supported color key has not been specified in the theme.
            Fix: Write a line of code with the printed key and give it a value.

        * RGB value out of range for key: '<key>'. Expects values from 0-255 ->
          -> Red is: <value>
          -> Green is: <value>
          -> Blue is: <value>
            Explanation: A RGB value for the specified key is out of range (<value> < 0 OR <value> > 255)
            Fix: Use a value in the valid range (0 to 255)

4. List of Supported Keys
    The following keys are supported by the MapCanvas
        * background -> The background of the MapCanvas
        * coastline -> The stroke (lines) of the Coastlines
