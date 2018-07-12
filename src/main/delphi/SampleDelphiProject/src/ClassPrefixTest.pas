unit ClassPrefixTest;

interface

type
    // Define the classes in this Unit at the very start for clarity
  TForm1 = Class;          // This is a forward class definition

  TFruit = Class(TObject)  // This is an actual class definition :
    // Internal class field definitions - only accessible in this unit
  private
    isRound  : Boolean;
    length   : single;
    width    : single;
    diameter : single;
    // Fields and methods only accessible by this class and descendants
  protected
    // Externally accessible fields and methods
  public
    // 2 constructors - one for round fruit, the other long fruit
    constructor Create(diameter : single);               overload;
    constructor Create(length : single; width : single); overload;
    // Externally accessible and inspectable fields and methods
  published
    // Note that properties must use different names to local defs
    property round : Boolean
      read   isRound;
    property len   : single
      read   length;
    property wide  : single
      read   width;
    property diam  : single
      read   diameter;
  end;

implementation

end.
