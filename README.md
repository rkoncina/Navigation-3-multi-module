A sample app using AndroidX Navigation 3
The app contains 3 screens and they're each in it's own Gradle module. In general there would be multiple screens per module.

The screens are:
- Home
- Feature
- Selection

## Home screen

<img width="280" alt="image" src="https://github.com/user-attachments/assets/88231f92-b77f-407a-9b10-2973f5cdad55" />

On the Home screen you can see 2 lists of integers and you can open the Selection screen for each list, by clicking on the text, to change the selection.
You also have the button to open the Feature screen

## Feature screen

<img width="271" alt="image" src="https://github.com/user-attachments/assets/c5b92d24-9d65-458e-9369-ba4cfd41c1d0" />

The feature screen is similar, also contains 2 lists of integers which you can change by opening the Selection screen. But you can't open another Feature screen from here.
The feature screen also tells you how many items are selected on the Home screen to simulate passing down data.

## Selection screen

<img width="265" alt="image" src="https://github.com/user-attachments/assets/37104dc6-5a3f-46a1-a2f8-166265a03426" />

The Selection screen is a list of integers from 1 to 5 and you can select/deselect them. 0, 1 or more can be selected. You confirm the selection using the Apply button.

---

Problem: how to pass the selection back to the previous screen?
Current implementation adds a callback to the navigation page object which is not serializable and hence can't survive Andorid configuration change.
