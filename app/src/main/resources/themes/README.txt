THE BASICS OF THEMES
0. Welcome
    This guide will teach you the basics of themes.

    1. Create a theme
    2. Keywords
        2.1 Language Keywords
        2.2 Explanation of Keywords
        2.3 Properties
    3. Writing some simple Keys
        3.1 Requirements
        3.2 Writing the code
    4. Warnings and Debugging
    5. List of Supported Keys

-------------------------------------------------------------------------------------

1. Basic information
    1.1 Create a Theme
    To create a theme, simply create a new file with '.mtheme' as the file extension.

    1.2 Basic Syntax
    The following syntax most be followed in order for correct parsing.
        - All code must be ended with a ';'
        - All places where a string object is expected, the string must be enclosed in '""'
        - The language is very sensitive and does not tolerate misplaced spaces.
            E.g. '"road"  =     {iColor = [0, 0, 0]};' is not accepted but '"road" = {iColor = [0, 0, 0]};' is.

2. Keywords
    2.1 Language Keywords
    A theme currently has 3 keywords covering the following:
        * Theme name
        * Key
        * Comment

    2.2 Explanation of Keywords
    * Name
        The name of the theme. It can consist of the letters A-Z (both upper and lowercase) and spaces.
        Syntax: name = "<name>";

    * Key
        A Key is the most important and fundamental keyword. This is what corresponds to an element.
        A Key is a string of lowercase letters from a-z including '_' (underscore) and excluding space enclosed in '""'.
        A Key has something called properties, which specifies how the element is drawn.
        Syntax: "<key>" = {PROPERTY}, ..., {PROPERTY};

    * Comment
        A comment is plain text which is ignored. Good for making things clear.
        Syntax: #<text>

    2.3 Properties
    A Key can have specific properties. Each property is enclosed in '{}' and separated by a ','.
    * Color
            Color is a property written in RGB format (values from 0 to 255). A color has two categories inner and outer color.
                - The inner color (iColor) is the color used for the inner bounds of a line.
                - The outer color (oColor) is the color used for the border of the line.
            Syntax:
                - {iColor = [255, 255, 255]}
                - {iColor = [255, 255, 255], oColor = [0, 0, 0]}

        * Width
            Width is a property consisting of a property pointer and a positive integer.
            A width property has two categories inner and outer width:
                - The inner width (iWidth) is the width used for the inner bounds of a line.
                - The outer width (oWidth) is the width used for the border of the line.

                NOTE: To see the difference between the inner and outer line, the outer must be bigger than the inner!
                Warning: If no width property is set, the default value is used (1).

           Syntax:
                - {iWidth = 1}
                - {iWidth = 1, oWidth = 2}

        * Style
            Style is a property consisting of a single string, specifying in what style the line has to be drawn e.g. dashed.
            The style string must be enclosed in '""'.
            Supported styles are:
                - "dotted" -> - - - -

            Warning: If no style is set, the default value is used (solid).

            Syntax:
                - {style = "dotted"}

        * Filled
            Filled is a property consisting of a single boolean value (true or false).

            NOTE: If filled is set to true, it fills the object with the inner color and ignores the outer.
            Warning: If no filled property is set, the default value is used (false);

            Syntax:
                - {filled = true}

3. Writing some Simple Keys
   3.1 Requirements
   Let's say we want to display a motorway object, a cycleway and a park with the following properties:
        - The motorway consists of a thick red line with a darker red as border.
        - The cycleway is thin and blue dotted.
        - The park is a large green area.

    3.2 Writing the Code
    Now let's make the motorway.
    It is thick and has a border, so we have to set both an inner and outer color. This implies us to also set the width.
    This gives us the following code: "motorway" = {iColor = [226, 122, 143], oColor = [213, 18, 88]}, {iWidth = 2, oWidth = 3};

    Now let's make the cycleway.
    It is thin and blue dotted, so we only have to set a inner color and a style.
    This gives us the following code: "cycleway" = {iColor = [0, 0, 255]}, {style = "dotted"};

    Now let's make the park.
    It is a large green area, so we have to set the filled property and give it a green color.
    This gives us the following code: "park" = {iColor = [205, 247, 201], oColor = [0, 0, 0]}, {filled = true};

4. Warnings and Debugging
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

5. List of Supported Keys
    The following keys are supported by the MapCanvas
        * background -> The background of the MapCanvas
        * coastline -> The stroke (lines) of the Coastlines
