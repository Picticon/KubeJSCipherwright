## Ciphers
A cipher is a JSON file that describes how to parse a recipe JSON file and output code. There is built in support for JSON and KubeJS.

You can add custom ciphers by making a JSON file and placing it inside the /data/cipherwright/ciphers namespace. You can also use sub-paths to organize.

### Format

`type` A string that points to the type of recipe this cipher handles.

`name` The display name for the cipher.

`category` The category for the cipher, most likely the name of the mod.

`inputs` `outputs` An array of slots. Both are handled very similar, except that inputs can be tags, ingredients, while outputs are only itemstacks. Fluids are handled like an itemstack for code generation.
                                      
`illustrations` An array of illustrations. These are icons and text.

`templates` An array of code templates. These appear under the "Clipboard" panel.
      
### Grid Objects

Grid objects are slots and illustrations. These require positioning in the work area.

`gridx` `gridy` `gridw` `gridh` Where on the 18x18 grid the object should be placed and sized. Decimals are allowed.

`left` `top` `w` `h` The exact placement of the object.

`repeat` Makes a shape of slots. Also triggers the array mode for the slot. The array mode will change the way the output is created.
* `oval` Accepts `segments` `gridRadiusWidth` and `gridRadiusHeight`. Radius is input as grid units and can be a decimal. Determines the width and shape of an oval, starting at the top position.
* `array` Accepts `rows` `cols` `rowspacing` `colspacing`. Builds a grid. Spacing is used if present as margins.
* `absolute` Accepts an array of numbers in the `coordinates` or `gridcoordinates` member. These are paired X,Y coordinates where odd indexes are X and even indexes are Y.



### Slots

These describe an input or output. How the information is parsed from the input JSON. What the user can do to edit the slot. And how the information is parsed into the output code.

`path` This is the identifier used for the slot and defines where in the JSON file to find the ingredient. This can be nested by using periods, like `input.item1` or inside an array `inputs[1]`. 

`path:count` This points to a different location for the count of the item. This is used when the id and count are in different places.

`type` Defines the type of input and output.
* `shaped` This triggers a shaped recipe type, such as used in the crafting table. `path` will point to the encoded pattern array, where each index is a row in the grid.
* `ingredient` This indicates the input can be either an item string, itemstack object, or a tag.
* `item` This indicates the input can be an item string or itemstack object only.
* `fluid` This indicates a fluidstack input.
* `itemstack` This indicates an itemstack or fluidstack output.

`key` This indicates where the item map is located for shaped recipes.

`index` This indicates the index in an array for the slot. Also triggers array mode for code output.

`single` Limits the count for this slot to 1.

`large` Makes the border for the slot larger, indicating an output.

`allowitem` `allowtag` `allowfluid` These change the types of inputs and outputs a slot will allow. By default, item and tags are true and fluid is false.

`members` This is an array of parameters for an item, such as chance. See Parameters for more information.

### Illustrations

`text` `formula` This adds a string to the work area. The only available formula is `ticks2seconds:path` which will attempt a tick count conversion of the input parameter.

`item` This displays the item or block on the work area. This is input as a resource location.

`right_arrow` `left_arrow` `up_arrow` `down_arrow` These are glyphs that can be used for information.

`scale` Used to shrink or expand glyphs and items.

`rightalign` If present in the `flags` field, will right align the text.

