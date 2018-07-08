unit ConstructorTest;

interface

implementation



var
  fruit  : TFruit;
  banana : TFruit;
  apple  : TApple;

type
T = class(TObject)

  public
    // Compliant
    constructor Create;
    constructor CreateNew;
    // Non compliant
    constructor Make;
    constructor Get;
  end;

begin
  // Create 3 different fruit objects
  fruit  := TFruit.Create;
  banana := TFruit.Create('Banana');
  apple  := TApple.Create('Pink Lady', 12);

  // See which of our objects are fruits
  if fruit  Is TFruit then ShowMessage(fruit.name  +' is a fruit');
  if banana Is TFruit then ShowMessage(banana.name +' is a fruit');
  if apple  Is TFruit then ShowMessage(apple.name  +' is a fruit');

  // See which objects are apples
  if fruit  Is TApple then ShowMessage(fruit.name   +' is an apple');
  if banana Is TApple then ShowMessage(banana.name  +' is an apple');
  if apple  Is TApple then ShowMessage(apple.name   +' is an apple');

end.
