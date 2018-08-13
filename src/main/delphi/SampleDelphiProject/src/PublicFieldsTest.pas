unit PublicFieldsTest;

interface

uses
  Forms, Dialogs, Classes, Controls, StdCtrls, SysUtils;

type
  // Define a base TSquare class :
  // It has private data that can only be set by a protected method
  TSquare = class
  private           // Only known to the parent class
    squareArea : Integer;
  protected         // Known to all classes in the hierarachy
    squareWidth, squareHeight : Integer;
    procedure setArea;
  public            // Known externally by class users
    property width  : Integer read squareWidth;
    property height : Integer read squareHeight;
    property area   : Integer read squareArea;
    violationField : Integer; // Non compliant
  published         // Known externally : has run time info also
    constructor Create(width, height : Integer);
  end;

  // Define a descendant type :
  // It must use the parent protected method to set the
  // private area of the square
  TChangeableSquare = class(TSquare)
  public
    procedure ChangeSize(newWidth, newHeight : Integer);
  end;

  // Define the form class used by this unit
  TForm1 = class(TForm)
    procedure FormCreate(Sender: TObject);
  private
    dummyString : String;
  public
    dummyVar : Integer; // Non-Compliant
  end;

implementation

end.